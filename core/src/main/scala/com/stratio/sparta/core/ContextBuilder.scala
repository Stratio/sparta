/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core

import com.stratio.sparta.core.utils.ClasspathUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

/**
  * Typeclass for type generators fix in "Rows" (`DStream[Row]`, `Dataset[Row]`, `Array[Row]`...)
  * whose goal is to abstract factories of [[WorkflowContext]] for the type.
  *
  * e.g: When working with `DStream[Row]`, you'll need to build a [[WorkflowContext]] with an streaming
  * context whereas when working with `Dataframe[Row]` there is no need to initialize it.
  *
  */
trait ContextBuilder[Underlying[Row]] extends Serializable {
  def buildContext(classpathUtils: ClasspathUtils, xDSession: XDSession)(
    streamingContextFactory: => StreamingContext
  ): WorkflowContext
}

object ContextBuilder {

  trait ContextBuilderImplicits {

    implicit val dStreamContextBuilder = new ContextBuilder[DStream] {
      override def buildContext(classpathUtils: ClasspathUtils, xDSession: XDSession)(
        streamingContextFactory: => StreamingContext
      ): WorkflowContext = WorkflowContext(classpathUtils, xDSession, Some(streamingContextFactory))
    }

    implicit val datasetContextBuilder = new ContextBuilder[Dataset] {
      override def buildContext(classpathUtils: ClasspathUtils, xDSession: XDSession)(
        streamingContextFactory: => StreamingContext
      ): WorkflowContext = WorkflowContext(classpathUtils, xDSession)
    }

    implicit val rddContextBuilder = new ContextBuilder[RDD] {
      override def buildContext(classpathUtils: ClasspathUtils, xDSession: XDSession)(
        streamingContextFactory: => StreamingContext
      ): WorkflowContext = WorkflowContext(classpathUtils, xDSession)
    }

  }

}
