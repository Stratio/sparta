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
