/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.oauth

import com.typesafe.config.ConfigFactory

import scala.util.Try

class Config(conf: com.typesafe.config.Config = ConfigFactory.load().getConfig("oauth2")) {


  val AuthorizeUrl: String = getDefaultString("url.authorize", Option(""))
  val accessTokenUrl: String = getDefaultString("url.accessToken", Option(""))
  val ProfileUrl: String = getDefaultString("url.profile", Option(""))
  val RedirectUrl: String = getDefaultString("url.callBack", Option(""))
  val LogoutUrl: String = getDefaultString("url.logout", Option(""))
  val indexPath: String = getDefaultString("url.onLoginGoTo", Option("/"))
  val ClientId: String = getDefaultString("client.id", Option(""))
  val ClientSecret: String = getDefaultString("client.secret", Option(""))
  val CookieName: String = getDefaultString("cookieName", Option("user"))
  val Enabled: Boolean = getDefaultBoolean("enable", false)
  val RoleName: String = getDefaultString("roleName",Option("roles"))
  val configuration = conf

  private def getDefaultBoolean(key: String, defaultValue: Boolean): Boolean = {
    Try(conf.getString(key).toBoolean).toOption match {
      case Some(x) => x
      case None => defaultValue
    }
  }

  private def getDefaultString(key: String, defaultValue: Option[String]): String = {
    val optionValue = Try(conf.getString(key)).toOption match {
      case Some(x) => Option(x)
      case None => defaultValue
    }
    optionValue.fold(throw new RuntimeException(s"$key is not defned"))(x => x)


  }
}