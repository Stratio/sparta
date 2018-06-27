/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.gosec.dyplon.plugins.sparta.dummy.management

//scalastyle:off
object Dummy extends App {

  def printHelp: Unit = {
    println("1)announcements: announcement plugin to gosec zk (only first time)")
    println("2)register: after announcement, register plugin to show in gosec-management (only first time)")
    println(s"3)policy ResourceType ResourceName Option(allowed) Option(recursive): create custom policy for user=$userId , verison =$ver")
  }

  val userId = "admin"
  val ver = "2.1.0-SNAPSHOT"
  val dummyPlugin: DummyInstance = new DummyInstance(userId = userId, manifestType = "sparta",
    manifestVersion = ver, instanceArg = Seq("sparta"))

  printHelp
  println(dummyPlugin.toString)
  dummyPlugin.listencommands()
}

//scalastyle:on
