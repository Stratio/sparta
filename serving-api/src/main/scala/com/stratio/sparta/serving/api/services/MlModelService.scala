/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.services

import akka.event.slf4j.SLF4JLogging
import com.stratio.intelligence.mlmodelrepository.client.MlModelsRepositoryClient
import com.stratio.sparta.core.helpers.SSLHelper
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.models.intelligence.IntelligenceModel
import com.typesafe.config.Config

import scala.util.{Failure, Success, Try}

class MlModelService() extends SLF4JLogging {

  private lazy val intelligenceConfig: Option[Config] = SpartaConfig.getIntelligenceConfig()

  private lazy val modelRepositoryUrl: Option[String] = Try {
    intelligenceConfig.get.getString(AppConstant.ModelRepositoryUrlKey)
  }.toOption.notBlank

  private lazy val modelRepositoryClient: Try[MlModelsRepositoryClient] = Try {
    val url = modelRepositoryUrl.getOrElse(throw new Exception("Intelligence model repository URL not found"))
    if (url.contains("https"))
      new MlModelsRepositoryClient(getOrCreateStandAloneXDSession(None), url, SSLHelper.getSSLContextV2(withHttps = true))
    else new MlModelsRepositoryClient(getOrCreateStandAloneXDSession(None), url)
  }

  def listModels(): Try[Seq[IntelligenceModel]] =
    Try {
      modelRepositoryClient match {
        case Success(client) =>
          client.getModelsMetadata.get.modelsMetadata.flatMap { modelMetadata =>
            if (modelMetadata.versions.exists(version => version.files.exists(file => file.serializationLib.toLowerCase.contains("spark"))))
              Option(IntelligenceModel(modelMetadata.modelName))
            else None
          }
        case Failure(e) =>
          log.warn(s"Intelligence model repository client not configured. ${e.getLocalizedMessage}")
          Seq.empty[IntelligenceModel]
      }
    }
}