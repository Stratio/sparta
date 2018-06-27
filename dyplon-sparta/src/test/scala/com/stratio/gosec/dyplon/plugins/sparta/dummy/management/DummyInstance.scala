/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.gosec.dyplon.plugins.sparta.dummy.management

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn.readLine
import scala.util.Try

import com.stratio.gosec.api.Systems.{AnnouncementSystem, InstanceSystem}
import com.stratio.gosec.api.audit.repository.{AuditRepositoryComponent, AuditRepositoryComponentImpl}
import com.stratio.gosec.api.config.ConfigComponentImpl
import com.stratio.gosec.api.utils.repository.zookeeper.ZkRepositoryComponentImpl
import com.stratio.gosec.dyplon.audit.LifeCycleSystem
import com.stratio.gosec.dyplon.facade.DyplonFacadeAuthorizerSystem
import com.stratio.gosec.dyplon.model.announcement.{AnnouncementID, PluginAnnouncement}
import com.stratio.gosec.dyplon.model.metadata.Scope
import com.stratio.gosec.dyplon.model.{Acl, _}

//scalastyle:off
class DummyInstance(userId: String, manifestType: String, manifestVersion: String, instanceArg: Seq[String])
  extends DyplonFacadeAuthorizerSystem with Logging {

  override def toString: String = s"DummyInstance userId=[$userId], type=[$manifestType], version=[$manifestVersion] " +
    s"instance=[$instanceArg]"

  val Workflows = Scope(`type` = "Workflows", actions = Seq(View, Create, Delete, Edit, Status))
  val Groups = Scope(`type` = "Groups", actions = Seq(View, Create, Delete, Edit, Status))
  val Catalog = Scope(`type` = "Catalog", actions = Seq(View, Select))
  val Environment = Scope(`type` = "Environment", actions = Seq(View, Create, Delete, Edit, Upload, Download))
  val Backup = Scope(`type` = "Backup", actions = Seq(View, Create, Delete, Upload, Execute))
  val Configuration = Scope(`type` = "Configuration", actions = Seq(View))
  val Plugin = Scope(`type` = "Plugin", actions = Seq(View, Delete, Upload, Download))
  val Template = Scope(`type` = "Template", actions = Seq(View, Create, Delete, Edit))
  val History = Scope(`type` = "History", actions = Seq(View))

  val dummyPlugin: PluginInstance = PluginInstance(`type` = manifestType, version = manifestVersion, instance = Some(instanceArg(0)),
    scope = Seq(Workflows, Groups, Catalog, Environment, Backup, Configuration, Plugin, Template, History))

  lazy val resourceService = dummyPlugin.`type` //sparta
  lazy val resourceInstance = instanceArg // This value is plugin.instance property, but in dummy is harcoded

  //policy admin-policy2 Workflows * admin

  def createPolicy(policyResourceType: String = "Workflows", policyResourceName: String, allow: Boolean = true, recursive: Boolean = true): Unit = {
    val resourcePolicy = Resource(resourceService, resourceInstance, policyResourceType, policyResourceName)
    val isAllowed = if (allow) Allow else Deny
    val acls = Seq(
      Acl(resourcePolicy, View, isAllowed, recursive),
      Acl(resourcePolicy, Create, isAllowed, recursive),
      Acl(resourcePolicy, Delete, isAllowed, recursive),
      Acl(resourcePolicy, Edit, isAllowed, recursive),
      Acl(resourcePolicy, Status, isAllowed, recursive),
      Acl(resourcePolicy, Upload, isAllowed, recursive),
      Acl(resourcePolicy, Download, isAllowed, recursive),
      Acl(resourcePolicy, Execute, isAllowed, recursive)
    )
    val policy = Policy(s"policy-random-${LocalDateTime.now().getSecond}", "Policy created by dummyPlugin",
      Option(Identities(Seq(userId))), Seq(dummyPlugin.`type`), instanceArg, acls)
    val result = policyService.save(policy)
    println(s"Policy $result created")
  }

  def deletePolicy(id: String): Unit = {
    val result = policyService.delete(id)
    println(s"Policy $id deleted = $result")
  }

  def listencommands() = {
    println(s"Enter command")
    while (true) {
      readLine(">") match {
        case "announcements" => announcementDummyPlugin()
        case "register" => Future {
          registerNewAnnouncements()
          AnnouncementSystem.announcementService.addListener(new EnrollmentListener())
        }
        case x if x startsWith "policy" => {
          val params = x.split(" ")
          createPolicy(params(1), params(2), Try(params(3).toBoolean).getOrElse(true), Try(params(4).toBoolean).getOrElse(true))
        }
        case "stop" => {
          System.exit(-1)
        }
        case _ => println("Unrecognized option")
      }
    }
  }

  def announcementDummyPlugin(): Unit = {
    Future {
      println(s"Register DummyPlugin [${dummyPlugin.toString}]")
      print(">")
      LifeCycleSystem(dummyPlugin).init
    }
    logger.info("End announcements")
  }

  private def toPluginInstance(pluginAnnouncement: PluginAnnouncement): PluginInstance =
    PluginInstance(
      pluginAnnouncement.service,
      pluginAnnouncement.version,
      pluginAnnouncement.scope,
      Option(pluginAnnouncement.instance),
      pluginAnnouncement.principal
    )

  private def toAnnouncementId(pluginAnnouncement: PluginAnnouncement): AnnouncementID =
    AnnouncementID(
      pluginAnnouncement.service,
      pluginAnnouncement.instance,
      pluginAnnouncement.version,
      pluginAnnouncement.id.getOrElse("")
    )

  def registerNewAnnouncements(): Unit = {
    val instanceService = PluginSystem.instanceService
    val announcements = AnnouncementSystem.announcementService.getAll()
    println(s"From Boot. Auto enrollment Plugins from Announcements [${announcements.mkString(",")}]")
    announcements.foreach { case announcement =>
      val pluginInstance = toPluginInstance(announcement)
      println(s"Saving pluginInstance [$pluginInstance]")

      Try(instanceService.save(pluginInstance))
        .getOrElse {
          Try(instanceService.update(pluginInstance))
          println(s"Updating pluginInstance [$pluginInstance]")
        }

      AnnouncementSystem.announcementService.delete(toAnnouncementId(announcement))
    }
  }
}

//scalastyle:on

object AuditSystem extends AuditRepositoryComponent
  with AuditRepositoryComponentImpl
  with ZkRepositoryComponentImpl
  with ConfigComponentImpl

object PluginSystem extends InstanceSystem

object AnnouncementSystem extends AnnouncementSystem