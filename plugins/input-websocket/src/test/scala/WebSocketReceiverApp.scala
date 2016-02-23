/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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


import com.stratio.sparkta.plugin.input.websocket.WebSocketReceiver
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * This isn't a test, is a little spark streaming program to check the receiver
 */
object WebSocketReceiverApp extends App {

  val conf = new SparkConf().setAppName("websocketTest").setMaster("local[2]")
  val ssc = new StreamingContext(conf, Seconds(10))
  val ds=  ssc.receiverStream(new WebSocketReceiver("ws://stream.meetup.com/2/rsvps",StorageLevel.MEMORY_ONLY))
  ds.foreachRDD(x=>
  println("data ----> "+x.collect().mkString(";"))
  )
  ssc.start()

  ssc.awaitTermination()

}
