/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.oauth

import akka.actor.ActorRef
import com.stratio.sparta.serving.api.actor.ClusterSessionActor.{NewSession, PublishRemoveSessionInCluster, PublishSessionInCluster, RemoveSession}
import com.stratio.sparta.serving.api.oauth.SessionStore._
import com.stratio.sparta.serving.core.constants.AppConstant
import spray.client.pipelining._
import spray.http.StatusCodes._
import spray.http.{DateTime, HttpCookie, HttpRequest, HttpResponse}
import spray.routing._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

trait OauthClient extends HttpService {

  implicit val ec: ExecutionContext = ExecutionContext.global

  import OauthClientHelper._

  val clusterSessionActor: ActorRef

  val configure = new Config

  private val cookieDuration: Long = sys.env.getOrElse("COOKIE_EXPIRATION_HOURS", "8").toLong * 3600000L

  private val instancePath: Option[String] = AppConstant.virtualPath

  private def authCookieWithExpiration(sessionId: String, expirationDelta: Long = cookieDuration) = HttpCookie(
    configure.CookieName,
    sessionId,
    Option(DateTime(System.currentTimeMillis) + expirationDelta),
    path = instancePath
  )

  def authorizeRedirect: StandardRoute = redirect(authorizeRq, Found)

  def indexRedirect: StandardRoute = redirect(configure.indexPath, Found)

  def logoutRedirect: StandardRoute = redirect(configure.LogoutUrl, Found)

  val authorized: Directive1[String] = {
    if (configure.Enabled) {
      optionalCookie(configure.CookieName) flatMap {
        case Some(x) => {
          getSession(x.content) match {
            case Some(cont: String) =>
              setCookie(authCookieWithExpiration(x.content)) & provide(cont)
            case None =>
              val sessionToRemove = RemoveSession(x.content)
              removeSession(sessionToRemove)
              clusterSessionActor ! PublishRemoveSessionInCluster(sessionToRemove)
              complete(Unauthorized, "")
          }
        }
        case None => complete(Unauthorized, "")
      }
    } else {
      val sessionId = getRandomSessionId
      val newSession = NewSession(sessionId, "*", Long.MaxValue)
      addSession(newSession)
      clusterSessionActor ! PublishSessionInCluster(newSession)
      setCookie(HttpCookie(
        configure.CookieName,
        sessionId,
        path = instancePath))
      provide("*")
    }
  }

  val secured: Directive1[String] = {
    if (configure.Enabled) {
      optionalCookie(configure.CookieName) flatMap {
        case Some(x) =>
          getSession(x.content) match {
            case Some(cont: String) => setCookie(authCookieWithExpiration(x.content)) & provide(cont)
            case None =>
              val sessionToRemove = RemoveSession(x.content)
              removeSession(sessionToRemove)
              clusterSessionActor ! PublishRemoveSessionInCluster(sessionToRemove)
              authorizeRedirect
          }
        case None => authorizeRedirect
      }
    } else {
      val sessionId = getRandomSessionId
      val newSession = NewSession(sessionId, "*", Long.MaxValue)
      addSession(newSession)
      clusterSessionActor ! PublishSessionInCluster(newSession)
      setCookie(HttpCookie(
        configure.CookieName,
        sessionId,
        path = instancePath))
      provide("*")
    }
  }


  val login: Route = (path("login") & get) {
    parameter("code") { code: String =>
      val (token, expires) = getToken(code)
      val sessionId = getRandomSessionId
      val newSession = NewSession(sessionId, getUserProfile(token), expires * 1000)
      addSession(newSession)
      clusterSessionActor ! PublishSessionInCluster(newSession)
      setCookie(authCookieWithExpiration(sessionId, cookieDuration)) {
        indexRedirect
      }
    }
  }

  val logout = path("logout") {
    get {
      optionalCookie(configure.CookieName) {
        case Some(x) => {
          val sessionToRemove = RemoveSession(x.content)
          removeSession(sessionToRemove)
          clusterSessionActor ! PublishRemoveSessionInCluster(RemoveSession(x.content))
          deleteCookie(configure.CookieName, path = "/") {
            logoutRedirect
          }
        }
        case None => logoutRedirect
      }
    }
  }

  def getToken(code: String): (String, Long) = {
    val tokenResponse: String = makeGetRq(tokenRq(code))
    val (token: String, expires: Long) = parseTokenRs(tokenResponse)
    (token, expires)
  }

  def getUserProfile(token: String): String = {
    val profileUrl = s"${configure.ProfileUrl}?access_token=$token"
    makeGetRq(profileUrl)
  }

  def makeGetRq(url: String): String = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val response = pipeline(Get(url))
    val plainResponse: HttpResponse = Await.result(response, Duration.Inf)
    plainResponse.entity.asString
  }


  val logRoute = login ~ logout

}