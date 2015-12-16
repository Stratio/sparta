/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.benchmark.generator.runners

import java.io.File

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.benchmark.generator.actors.{FileReader, MetricsActor, DataGeneratorActor}
import com.stratio.kafka.benchmark.generator.kafka.KafkaProducer
import com.typesafe.config.ConfigFactory
import kafka.producer.Producer
import org.apache.log4j.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Success, Try}

//scalastyle:off
object GeneratorRunner {
  
  val L = Logger.getLogger(GeneratorRunner.getClass)
  val pathToStartTime = "/home/gschiavon/desarrollo/stratio/benchmark/metrics/processing_start_time.csv"
  val pathToEndTime = "/home/gschiavon/desarrollo/stratio/benchmark/metrics/processing_end_time.csv"
  val pathToEvents = "/home/gschiavon/desarrollo/stratio/benchmark/metrics/received_records.csv"
  var producer: Producer[String, String] = _
  var topic: String = _
  val fullReportFile = new File("/tmp/FullReport.csv")

  def main(args: Array[String]) {
    if(args.size == 0) {
      println("Use: java -jar twitter-election.jar <config file>")
      System.exit(1)
    }

    implicit val system: ActorSystem = ActorSystem("DataGenerator")

//    val actor1 = system.actorOf(Props(new FakenatorActor), "fakenatorActor1")

    Try(ConfigFactory.parseFile(new File(args(0)))) match {
      case Success(config) =>

        val numEvents = config.getInt("numberOfEvents")
        val numOfInstances = config.getInt("numberOfInstances")
        val eventsPerInstance = numEvents / numOfInstances
        topic = config.getString("topic")

        implicit val timeout = Timeout(60.second)


        //val startTime = System.currentTimeMillis()

        val actors: Seq[ActorRef] = (1 to numOfInstances).map { i =>
          val producer = KafkaProducer.getInstance(config)
          system.actorOf(Props(new DataGeneratorActor(producer)), s"GeneratorActor$i")
        }

//        val response: Seq[Future[Finish]] = actors.map { actor =>
//          (actor ?  CreateInstance(1, eventsPerInstance)).mapTo[Finish]
//        }

        val startTimeActor = system.actorOf(Props(new MetricsActor(pathToStartTime)), "StartTimeActor")
        val startTime =
          (startTimeActor ? FileReader(pathToStartTime)).mapTo[Map[String, String]]

        val endTimeActor = system.actorOf(Props(new MetricsActor(pathToEndTime)), "EndTimeActor")
        val endTime =
          (endTimeActor ? FileReader(pathToEndTime)).mapTo[Map[String, String]]

        val receivedEventActor = system.actorOf(Props(new MetricsActor(pathToEvents)), "ReceivedEventActor")
        val events =
          (receivedEventActor ? FileReader(pathToEvents)).mapTo[Map[String, String]]


        val differences: Future[List[(Double, String)]] =
          (for {
            mapStart <- startTime
            mapEnd <- endTime
            mapEvents <- events
          } yield {
            (mapStart.toList ++ mapEnd.toList ++ mapEvents.toList)
              .groupBy(_._1)
              .map( {
              value =>
               // println(value)
              (((value._2(1)._2.toString.toDouble - value._2(0)._2.toString.toDouble) / 1000F), value._2(2)._2.toString)
            }
              )
          }).map(_.toList)


       //differences.map(_.map(addValues))

        differences.onComplete {
//          case Success(result) => result.map(values => addValues(values._1, values._2))
          case Success(result) => printToFile(fullReportFile){p => result.foreach(p.println)}
          case _ => println("ERROR")
        }




        val data = Array("Five","strings","in","a","file!")
        printToFile(new File("/tmp/example.txt")) { p =>
          data.foreach(p.println)
        }





//        val fList: Future[Seq[Finish]] = Future.sequence(response)
//
//        fList.onComplete { _ =>
//          println("Finished!")
//          val stopTime = System.currentTimeMillis()
//          val finalTime: Float = ((stopTime - startTime)/1000F)
//          println(s"$finalTime seconds to send $numEvents events with $numOfInstances instances to kafka")
//          //system.shutdown()
//        }
//
//      case Failure(exception) => {
//        System.exit(1)
//      }
    }
  }
  def addValues(time: Double, events: String): Unit = {
    println(s"$time seconds to process $events events")
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }

  //scalastyle:on
}
