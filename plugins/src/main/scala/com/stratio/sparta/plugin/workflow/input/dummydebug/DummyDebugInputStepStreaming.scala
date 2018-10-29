/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.dummydebug

import java.io.{Serializable => JSerializable}

import akka.actor.{ActorSystem, Cancellable}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.serving.core.factory.SparkContextFactory
import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.scheduler.{StreamingListener, StreamingListenerBatchCompleted}
import org.apache.spark.streaming.test.DebugDStream

import scala.concurrent.duration._


class DummyDebugInputStepStreaming(
                                    name: String,
                                    outputOptions: OutputOptions,
                                    ssc: Option[StreamingContext],
                                    xDSession: XDSession,
                                    properties: Map[String, JSerializable])
  extends DummyDebugInputStep[DStream](name, outputOptions, ssc, xDSession, properties)
    with SLF4JLogging {

  var stopApplication : Boolean = false

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[DStream] = {
    lazy val debugDStream = new DebugDStream(ssc.get, createDistributedMonadRDDwithSchema()._1.ds)

    ssc.get.addStreamingListener(new StreamingListenerStop)

    var lastFinishTask: Option[Cancellable] = None
    val schedulerSystem =
      ActorSystem("SchedulerSystem",ConfigFactory.load(ConfigFactory.parseString("akka.daemonic=on")))
    import scala.concurrent.ExecutionContext.Implicits.global
    lastFinishTask = Option(schedulerSystem.scheduler.schedule(0 milli, 50 milli)({
      if (stopApplication) {
        SparkContextFactory.stopStreamingContext()
        lastFinishTask.foreach(_.cancel())
      }
    }))

    debugDStream
  }


  class StreamingListenerStop extends StreamingListener {
    override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
      stopApplication = true
    }
  }
}


