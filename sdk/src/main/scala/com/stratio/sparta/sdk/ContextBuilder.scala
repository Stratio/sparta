/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.sdk

import com.stratio.sparta.sdk.utils.ClasspathUtils
import com.stratio.sparta.sdk.workflow.step.WorkflowContext
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
