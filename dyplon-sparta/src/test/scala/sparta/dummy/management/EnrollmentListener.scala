/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package sparta.dummy.management

import com.stratio.gosec.api.announcement.service.AnnouncementService
import com.stratio.gosec.api.instance.service.InstanceService
import com.stratio.gosec.dyplon.model.announcement.{AnnouncementID, PluginAnnouncement}
import com.stratio.gosec.dyplon.model.listener.{Event, Listener, ListenerCallback, WriteEvent}
import com.stratio.gosec.dyplon.model.{Logging, PluginInstance}

import scala.util.Try

//scalastyle:off
class EnrollmentListener(announcementService: AnnouncementService = AnnouncementSystem.announcementService,
                         instanceService: InstanceService = PluginSystem.instanceService
                        ) extends Listener with Logging {

  override def handleEvent(event: Event, callBack: Option[ListenerCallback]): Unit =
    event match {
      case event: WriteEvent =>
       println(s"Event of type [$event] will be processed  ")
        val announcements = announcementService.getAll()
        println(s"Auto enrollment Plugins from Announcements [${announcements.mkString(",")}]")
        announcements.foreach { case announcement =>
          val pluginInstance = toPluginInstance(announcement)
          val saveTry = Try(instanceService.save(pluginInstance))
          println(s"Saving: ${saveTry.toString}")
          saveTry.getOrElse {
            val updateTry = Try(instanceService.update(pluginInstance))
            println(s"Updating: ${updateTry.toString}")
          }
          announcementService.delete(toAnnouncementId(announcement))
        }
      case _ => logger.info(s"Event of type [$event] will NOT be processed ")
    }

  def toAnnouncementId(pluginAnnouncement: PluginAnnouncement): AnnouncementID =
    AnnouncementID(
      pluginAnnouncement.service,
      pluginAnnouncement.instance,
      pluginAnnouncement.version,
      pluginAnnouncement.id.getOrElse("")
    )

  def toPluginInstance(pluginAnnouncement: PluginAnnouncement): PluginInstance =
    PluginInstance(
      pluginAnnouncement.service,
      pluginAnnouncement.version,
      pluginAnnouncement.scope,
      Option(pluginAnnouncement.instance),
      pluginAnnouncement.principal
    )
}
//scalastyle:on