import java.io.File

import com.stratio.sparkta.driver.configuration.{AggregationPoliciesConfiguration, GeneralConfiguration}
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.factory.StreamingContextFactory
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.io.Source

/**
 * Created by ajnavarro on 2/10/14.
 */
object Main {
  def main(args: Array[String]) {
    implicit val formats = DefaultFormats

    //get jsons in a specified folder
    if (args.size != 1) {
      throw new DriverException("Usage: \n - param 1: Path with configuration files.")
    }

    // convert jsons to objects
    val files = new File(args(0)).listFiles.filter(_.getName.endsWith(".json"))
    val aggregationPolicies: Seq[AggregationPoliciesConfiguration] = files.map(f =>
      parse(Source.fromFile(f).getLines().mkString)
        .extract[AggregationPoliciesConfiguration]
    )

    //TODO read general configuration
    val generalConfiguration = new GeneralConfiguration("local[2]", "")

    //TODO generate each context
    val contexts = aggregationPolicies.map(policy => {
      StreamingContextFactory.getStreamingContext(policy, generalConfiguration)
    })

    //TODO start all context
    contexts.foreach(_.start())
  }
}
