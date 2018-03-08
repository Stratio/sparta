/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.marathon

import java.net.HttpCookie

import akka.event.slf4j.SLF4JLogging
import com.stratio.tikitakka.common.util.ConfigComponent

import scala.util.{Failure, Success, Try}
import scalaj.http.{Http, HttpResponse}

object OauthTokenUtils extends SLF4JLogging {

  lazy private val dcosAuthCookieName = "dcos-acs-auth-cookie"
  lazy private val ssoUriField = "sparta.marathon.sso.uri"
  lazy private val usernameField = "sparta.marathon.sso.username"
  lazy private val passwordField = "sparta.marathon.sso.password"
  lazy private val clientIdField = "sparta.marathon.sso.clientId"
  lazy private val redirectUriField = "sparta.marathon.sso.redirectUri"
  lazy private val retriesField = "sparta.marathon.sso.retries"
  lazy private val defaultRetries = 10

  lazy private val ssoUri = ConfigComponent.getString(ssoUriField)
    .getOrElse(throw new Exception("SSO Uri not defined"))
  lazy private val username = ConfigComponent.getString(usernameField)
    .getOrElse(throw new Exception("username not defined"))
  lazy private val password = ConfigComponent.getString(passwordField)
    .getOrElse(throw new Exception("password not defined"))
  lazy private val clientId = ConfigComponent.getString(clientIdField)
    .getOrElse(throw new Exception("clientId not defined"))
  lazy private val redirectUri = ConfigComponent.getString(redirectUriField)
    .getOrElse(throw new Exception("redirectUri not defined"))
  lazy private val maxRetries = Try(ConfigComponent.getInt(retriesField).get)
    .getOrElse(defaultRetries)

  @volatile
  private var currentToken: Option[HttpCookie] = None


  //scalastyle:off
  def getToken: HttpCookie = {
    synchronized {
      currentToken match {
        case Some(t) if !t.hasExpired =>
          log.debug(s"Cookie not expired with maxAge: ${t.getMaxAge}")
          t
        case Some(t) if t.hasExpired =>
          log.debug(s"Cookie expired with maxAge ${t.getMaxAge}")
          tokenWithExpiration
        case _ =>
          log.debug(s"Getting new cookie")
          tokenWithExpiration
      }
    }
  }

  def expireToken(): Unit = currentToken.foreach(cookie => cookie.setMaxAge(0))

  private def tokenWithExpiration: HttpCookie = {
    Try(retryGetToken(1)(retrieveTokenFromSSO)) match {
      case Success(token) =>
        log.debug(s"Cookie obtained successful")
        token.setMaxAge(5 * 60 * 60) //5 hours
        log.debug(s"Modified cookie MaxAge: ${token.getMaxAge}")
        currentToken = Option(token)
        token
      case Failure(ex: Throwable) =>
        currentToken = None
        throw new Exception(s"ERROR: ${ex.getMessage}." +
          s" For further information, please consult the application log")
    }
  }

  private def retryGetToken(numberCurrentRetries: Int)(getTokenFn: => Try[Seq[HttpCookie]]): HttpCookie =
    if (numberCurrentRetries <= maxRetries) {
      retrieveTokenFromSSO match {
        case Success(tokenCookie: Seq[HttpCookie]) => {
          val dcosCookie = tokenCookie.filter(cookie =>
            cookie.getName.equalsIgnoreCase(dcosAuthCookieName))
          if (dcosCookie.nonEmpty) {
            log.debug(
              s"""Marathon Token "$dcosAuthCookieName" correctly retrieved """ +
                s"at retry attempt n. $numberCurrentRetries")
            dcosCookie.head
          }
          else {
            log.debug(s"Retry attempt n. $numberCurrentRetries :" +
              s"Error trying to recover the Oauth token: cookie $dcosAuthCookieName not found")
            retryGetToken(numberCurrentRetries + 1)(getTokenFn)
          }
        }
        case Failure(ex: Throwable) =>
          log.debug(s"Retry attempt n. $numberCurrentRetries :" +
            s" Error trying to recover Oauth token: ${ex.getMessage}")
          retryGetToken(numberCurrentRetries + 1)(getTokenFn)
        case _ =>
          log.debug(s"Retry attempt n. $numberCurrentRetries :" +
            s" Token not found in last response")
          retryGetToken(numberCurrentRetries + 1)(getTokenFn)
      }
    } else {
      throw new Exception(s"It was not possible to recover the Oauth token " +
        s"$dcosAuthCookieName after $maxRetries retries")
    }

  private def retrieveTokenFromSSO: Try[Seq[HttpCookie]] =
    Try {
      // First request (AUTHORIZE)
      val authRequest = s"$ssoUri/oauth2.0/authorize?redirect_uri=$redirectUri&client_id=$clientId"
      log.debug(s"1. Request to : $authRequest")
      val authResponse = Http(authRequest).asString
      val JSESSIONIDCookie = getCookie(authResponse)

      // Second request (Redirect to LOGIN)
      val redirectToLogin = extractRedirectUri(authResponse)
      log.debug(s"2. Redirect to : $redirectToLogin with JSESSIONID cookie")
      val postFormUri = Http(redirectToLogin).cookies(JSESSIONIDCookie).asString
      val (lt, execution) = extractLTAndExecution(postFormUri.body)

      // Third request (POST)
      val loginPostUri = s"$ssoUri/login?service=$ssoUri/oauth2.0/callbackAuthorize"
      log.debug(s"3. Request to $loginPostUri with JSESSIONID cookie")
      val loginResponse = Http(loginPostUri)
        .cookies(JSESSIONIDCookie)
        .postForm(createLoginForm(lt, execution))
        .asString

      val CASPRIVACY_AND_TGC_COOKIES = getCookie(loginResponse)

      // Fourth request (Redirect from POST)
      val callbackUri = extractRedirectUri(loginResponse)
      log.debug(s"4. Redirect to : $callbackUri with JSESSIONID, CASPRIVACY and TGC cookies")
      val ticketResponse = Http(callbackUri).cookies(CASPRIVACY_AND_TGC_COOKIES union JSESSIONIDCookie).asString

      // Fifth request (Redirect with Ticket)
      val clientRedirectUri = extractRedirectUri(ticketResponse)
      log.debug(s"5. Redirect to : $clientRedirectUri with JSESSIONID, CASPRIVACY and TGC cookies")
      val tokenResponse =
        Http(clientRedirectUri).cookies(CASPRIVACY_AND_TGC_COOKIES union JSESSIONIDCookie).asString

      getCookie(tokenResponse)
    }

  private def extractRedirectUri(response: HttpResponse[String]): String =
    response.headers.get("Location").get.head

  private def extractLTAndExecution(body: String): (String, String) = {
    val ltLEftMAtch = "name=\"lt\" value=\""
    val lt1 = body.indexOf(ltLEftMAtch)
    val prelt = body.substring(lt1 + ltLEftMAtch.length)
    val lt = prelt.substring(0, prelt.indexOf("\" />")).trim

    val executionLEftMAtch = "name=\"execution\" value=\""
    val execution1 = body.indexOf(executionLEftMAtch)
    val execution = body.substring(execution1 + executionLEftMAtch.length).split("\"")(0)

    (lt, execution)
  }

  private def createLoginForm(lt: String, execution: String): Seq[(String, String)] =
    Seq(
      "lt" -> lt,
      "_eventId" -> "submit",
      "execution" -> execution,
      "submit" -> "LOGIN",
      "username" -> username,
      "password" -> password
    )

  private def getCookie(response: HttpResponse[String]): Seq[HttpCookie] = {
    response.headers.get("Set-Cookie") match {
      case Some(cookies) =>
        cookies.map { cookie =>
          val cookieFields = cookie.split(";")(0).split("=")
          new HttpCookie(cookieFields(0), cookieFields(1))
        }.distinct
      case None => Seq.empty[HttpCookie]
    }
  }
}

