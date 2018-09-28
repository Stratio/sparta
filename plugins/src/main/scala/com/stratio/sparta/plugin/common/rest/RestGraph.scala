/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.rest

import java.net.URL

import akka.event.slf4j.SLF4JLogging
import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.{Marshal, PredefinedToEntityMarshallers}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.{ClosedShape, OverflowStrategy, Supervision}
import akka.stream.Supervision.stoppingDecider
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import com.stratio.sparta.core.enumerators.WhenRowError
import com.stratio.sparta.core.models.TransformationStepManagement
import com.stratio.sparta.plugin.common.rest.RestUtils.WithPreprocessing
import org.apache.spark.sql.{Row, RowFactory}

import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.util.{Failure, Try}


object RestGraph {

  def apply(restConfig: RestConfig, restUtils: SparkExecutorRestUtils): RestGraph = new RestGraph(restConfig, restUtils)

}

class RestGraph private (
                          private val restConfig: RestConfig,
                          private val restUtils: SparkExecutorRestUtils
                        ) extends SLF4JLogging {

  import scala.concurrent.ExecutionContext.Implicits.global


  def createTransformationGraph(rowsIterator: Iterator[Row],
                                uriMapping: Map[String, WithPreprocessing],
                                bodyMapping: Map[String, WithPreprocessing],
                                transformationStepsManagement: Option[TransformationStepManagement]
                               ):
  RunnableGraph[Future[Seq[(String, Row)]]] = {

    /**
      * This sink will output all the correctly processed rows
      **/
    val extractRow: Sink[Future[(String, Row)], Future[Seq[(String, Row)]]] =
      Flow[Future[(String, Row)]]
        .mapAsyncUnordered(1) (identity)
        .toMat {
          Sink.fold(Seq.empty[(String, Row)]) { case (acc, elem) => elem +: acc }
        }(Keep.right)

    createGraph(rowsIterator, transformationStepsManagement, extractRow, uriMapping, bodyMapping)

  }

  def createInputGraph(rowsIterator: Iterator[Row],
                                uriMapping: Map[String, WithPreprocessing],
                                bodyMapping: Map[String, WithPreprocessing],
                                transformationStepsManagement: Option[TransformationStepManagement]
                               ):
  RunnableGraph[Future[Seq[(String, Row)]]] = {

    /**
      * This sink will output all the correctly processed rows
      **/
    val extractRow: Sink[Future[(String, Row)], Future[Seq[(String, Row)]]] =
      Flow[Future[(String, Row)]]
        .mapAsyncUnordered(1) (identity)
        .toMat {
          Sink.fold(Seq.empty[(String, Row)]) { case (acc, elem) => elem +: acc }
        }(Keep.right)

    createGraph(rowsIterator, transformationStepsManagement, extractRow, uriMapping, bodyMapping, requireProcessing = false)

  }


  def createOutputGraph(rowsIterator: Iterator[Row],
                        uriMapping: Map[String, WithPreprocessing],
                        bodyMapping: Map[String, WithPreprocessing],
                        transformationStepsManagement: Option[TransformationStepManagement])
  : RunnableGraph[Future[Done]]
  = {
    /**
      * This sink will discard the incoming data
      **/
    val discardRow: Sink[Future[(String, Row)], Future[Done]] =
      Flow[Future[(String, Row)]]
        .mapAsyncUnordered(1) { futureElement => for {elem <- futureElement} yield elem }
        .toMat(Sink.ignore)(Keep.right)

    createGraph(rowsIterator, transformationStepsManagement, discardRow, uriMapping, bodyMapping)

  }



  private def createGraph[U] (rowsIterator: Iterator[Row],
                      transformationStepsManagement: Option[TransformationStepManagement],
                      sink: Sink[Future[(String, Row)], Future[U]],
                      uriMapping: Map[String,WithPreprocessing],
                      bodyMapping: Map[String, WithPreprocessing],
                      requireProcessing : Boolean = true) = {

    import restUtils.Implicits._

    lazy val urlUnwrapped: String = restConfig.url.get
    // --- --- Properties for graph customization --- ---
    val maxRequest = restUtils.Implicits.aSystem.settings.config.getInt("akka.http.host-connection-pool.max-open-requests")
    val maxConnections = restUtils.Implicits.aSystem.settings.config.getInt("akka.http.host-connection-pool.max-connections")
    val maxConcurrentPartitions = Runtime.getRuntime.availableProcessors() // TODO spark.task.cores

    lazy val supervisionStreamDecider =
      if (transformationStepsManagement.isEmpty) stoppingDecider
      else supervisionDeciderPolicySparta(transformationStepsManagement.get)


    def supervisionDeciderPolicySparta(transformationStepsMgmt: TransformationStepManagement): Supervision.Decider = {
      errorThrown: Throwable =>
        transformationStepsMgmt.whenRowError match {
          case WhenRowError.RowError =>
            log.error("Unhandled exception in stream", errorThrown)
            Supervision.stop
          case WhenRowError.RowDiscard =>
            log.info(s"Handled exception occurred in stream: ${errorThrown.getLocalizedMessage}")
            Supervision.resume
        }
    }
    val buffer: Option[Int] =
      if ( (maxRequest / maxConnections) <  maxConcurrentPartitions) {
        val newLimit =
          maxRequest / maxConcurrentPartitions match {
            case limit if limit <= 0 => Some(1)
            case limit => Some(limit)
          }
        log.warn(
          "akka.http.host-connection-pool.max-open-requests should be higher than (maxConcurrentPartitions * akka.http.host-connection-pool.max-connections)" +
            s"Reverting to max-connections: $newLimit")
        newLimit
      } else None


    // --- --- Graph steps definition & drawing of graph --- ---

    /**
      * Our source is an iterator traversing the incoming RDD/DStream, we set here also the backpressure timeout
      **/
    val baseSourceRDD: Source[Row, NotUsed] =
      Source.fromIterator[Row](() => rowsIterator)


    val sourceRDD: Source[Row, NotUsed] =
      buffer.fold(baseSourceRDD){ bufferLimit => baseSourceRDD.buffer(bufferLimit, OverflowStrategy.backpressure)}

    def marshallingPhase(requireProcessing: Boolean): Flow[Row, (String, Future[MessageEntity], Row), NotUsed] =
      Flow[Row].map { row =>

        val processedUrl: String =
          if (requireProcessing) RestUtils.replaceInputFields(row, uriMapping, urlUnwrapped)
          else restConfig.url.get

        val processedBody: Option[String] =
          if (requireProcessing)
            restConfig.bodyString.map { bodyStr => RestUtils.replaceInputFields(row, bodyMapping, bodyStr)}
          else restConfig.bodyString

        log.debug(s"Formatted URL: $processedUrl")
        log.debug(s"Formatted body string: ${processedBody.getOrElse("Empty body string")}")

        processedBody.map{ processedString =>
          val marshaller = restConfig.bodyFormat.collect{
            case BodyFormat.JSON => PredefinedToEntityMarshallers.stringMarshaller(MediaTypes.`application/json`)
          }.getOrElse(PredefinedToEntityMarshallers.StringMarshaller)

          (processedUrl, Marshal(processedString).to[RequestEntity](marshaller, implicitly[ExecutionContext]), row)

        }.getOrElse((processedUrl, Future.successful(HttpEntity.Empty), row))
      }

    val sendRequestPhase: Flow[(String, Future[MessageEntity], Row), (HttpRequest, Row), NotUsed] =
      Flow[(String, Future[MessageEntity], Row)].mapAsync(1){
        case (processedUrl, futureReqEntity, row) =>
          futureReqEntity.map(reqEntity =>
            HttpRequest(restConfig.method, processedUrl:Uri, restConfig.headers, entity = reqEntity) -> row)
      }

    /**
      * This component "dispatches the incoming HTTP requests to the per-ActorSystem pool of outgoing
      * HTTP connections to the given target host endpoint".
      * TL;DR It automaGically transforms the incoming HTTPRequests into HttpResponse
      **/
    val poolConnection: Flow[(HttpRequest, Row), (Try[HttpResponse], Row), Http.HostConnectionPool] = {

      val url = new URL(RestUtils.ReplaceableFiledRegex.replaceAllIn(urlUnwrapped, "fakepath")) // TODO regex
      if (url.getProtocol == "https") {
        restUtils.getOrCreatePool(url.getHost, if (url.getPort < 0) 443 else url.getPort, isHttps = true)
      } else {
        restUtils.getOrCreatePool(url.getHost, if (url.getPort < 0) 80 else url.getPort)
      }
    }.idleTimeout(restConfig.requestTimeout.get).recover{
      case timeoutExc: TimeoutException =>
        log.error(s"Request timeout: The server did not respond after ${restConfig.requestTimeout.get}", timeoutExc)
        Failure(timeoutExc) -> RowFactory.create(Array.empty)
    }

    /**
      * In this step we try to unmarshall the response and we send it as string to the sink
      * together with the old row
      **/
    val unmarshallingPhase: Flow[(Try[HttpResponse], Row), Future[(String, Row)], NotUsed] =
      Flow[(Try[HttpResponse], Row)].map {
        case (tryResponse, relatedRow) =>
          tryResponse.map { httpResponse =>
            if (restConfig.statusCodeWhitelist.contains(httpResponse.status)) {
              for {
                stringUnmarshalled <- Unmarshal(httpResponse.entity).to[String]
              } yield (stringUnmarshalled, relatedRow)
            } else {
              httpResponse.discardEntityBytes()
              Future.failed(new RuntimeException(s"Rejected response with status code [${httpResponse.status}]. Check the defined whitelist"))
            }
          }.get
      }


    def graph[U](sink: Sink[Future[(String, Row)], Future[U]]): RunnableGraph[Future[U]] =
      RunnableGraph.fromGraph(GraphDSL.create(sink) {
        implicit builder =>
          sink =>
            import GraphDSL.Implicits._

            val marshallingPhaseGraph = builder.add(marshallingPhase(requireProcessing))
            val requestGraph = builder.add(sendRequestPhase)
            val poolGraph = builder.add(poolConnection)
            val unmarshallingGraph = builder.add(unmarshallingPhase)
            sourceRDD ~> marshallingPhaseGraph ~> requestGraph ~> poolGraph ~> unmarshallingGraph ~> sink.in
            ClosedShape
      }).withAttributes(supervisionStrategy(supervisionStreamDecider))

    graph(sink)
  }
}