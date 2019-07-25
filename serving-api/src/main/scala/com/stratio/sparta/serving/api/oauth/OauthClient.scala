/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.oauth

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparta.serving.api.actor.ClusterSessionActor._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.authorization.GosecUser
import spray.client.pipelining._
import spray.http.{DateTime, HttpCookie, HttpRequest, HttpResponse}
import spray.http.StatusCodes._
import spray.routing._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Success

trait OauthClient extends HttpService with SLF4JLogging {

  implicit val ec: ExecutionContext = ExecutionContext.global

  import OauthClientHelper._

  implicit val timeout: Timeout

  val clusterSessionActor: ActorRef

  val configure = new Config

  private val cookieDuration: Long = sys.env.getOrElse("COOKIE_EXPIRATION_HOURS", "8").toLong * 3600000L

  private def now: Long = System.currentTimeMillis

  private def getCookieExpirationTime: Long = now + cookieDuration

  private val instancePath: Option[String] = AppConstant.virtualPath

  private def authorizedCookie(sessionId: String, expiration: Long) = HttpCookie(
    name = configure.CookieName,
    content = sessionId,
    expires = Option(DateTime(expiration)),
    path = instancePath
  )

  def authorizeRedirect: StandardRoute = redirect(authorizeRq, Found)

  def indexRedirect: StandardRoute = redirect(configure.indexPath, Found)

  def logoutRedirect: StandardRoute = redirect(configure.LogoutUrl, Found)

  def unauthorizeRequest: StandardRoute = complete(Unauthorized, "")

  def forbiddenRequest: StandardRoute = complete(ForbiddenTemplate)

  val authorized: Directive1[String] = {
    optionalCookie(configure.CookieName) flatMap {
      case Some(httpCookie) =>
        val sessionId = httpCookie.content
        onComplete(getAndValidateIdentity(sessionId)) flatMap {
          case Success(Some(SessionInfo(identity, expiration))) =>
            setCookie(authorizedCookie(sessionId, expiration)) & provide(identity)
          case _ => unauthorizeRequest
        }
      case None => unauthorizeRequest
    }
  }

  val secured: Directive1[String] = {
    optionalCookie(configure.CookieName) flatMap {
      case Some(httpCookie) =>
        val sessionId = httpCookie.content
        onComplete(getAndValidateIdentity(sessionId)) flatMap {
          case Success(Some(SessionInfo(identity, expiration))) =>
            setCookie(authorizedCookie(sessionId, expiration)) & provide(identity)
          case _ =>
            authorizeRedirect
        }
      case None => authorizeRedirect
    }
  }

  val login: Route = (path("login") & get) {
    parameter("code") { code: String =>
      val (token, expires) = getToken(code)
      val sessionId = getRandomSessionId
      val ssoUserProfile = getUserProfile(token)
      val expiration = now + (expires * 1000)
      val newSession = NewSession(sessionId, ssoUserProfile, expiration)

      val ssoUserInfo = GosecUser.jsonToDto(ssoUserProfile)
      log.debug(s"Login attempted from user($ssoUserInfo)")

      val isAuthorized = AppConstant.EosTenant.forall { instanceEosTenant =>
        ssoUserInfo.flatMap(_.tenant).contains(instanceEosTenant)
      }

      if (isAuthorized) {
        onComplete(createNewIdentity(newSession)) {
          case Success(Some(id)) => setCookie(authorizedCookie(id, expiration))(indexRedirect)
          case _ => forbiddenRequest
        }
      } else forbiddenRequest
    }
  }

  val logout = path("logout") {
    get {
      optionalCookie(configure.CookieName) {
        case Some(httpCookie) =>
          val sessionId = httpCookie.content
          clusterSessionActor ! RemoveSession(sessionId)
          deleteCookie(configure.CookieName, path = "/")(logoutRedirect)
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

  private def getAndValidateIdentity(sessionId: String): Future[Option[SessionInfo]] = {
    (clusterSessionActor ? GetSession(sessionId))
      .mapTo[Option[SessionInfo]]
      .map {
        case Some(SessionInfo(identity, expiration)) if expiration >= now =>
          // Valid identity and expiration, refresh the session and return identity
          val cookieExpiration = getCookieExpirationTime
          clusterSessionActor ! RefreshSession(sessionId, identity, cookieExpiration)
          Some(SessionInfo(identity, cookieExpiration))
        case Some(_:SessionInfo)  =>
          // Cookie expired, remove from the session list
          clusterSessionActor ! RemoveSession(sessionId)
          None
        case _ => None
      }
  }

  private def createNewIdentity(newSession: NewSession): Future[Option[String]] = {
    (clusterSessionActor ? newSession)
      .mapTo[NewSessionCreated]
      .map {
        case NewSessionCreated(sessionId) => Some(sessionId)
        case _ => None
      }
  }

  val logRoute = login ~ logout

}