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

package com.stratio.sparkta.serving.core.messages

import java.io.File

import org.json4s._

import scala.util.Try


object JobServerMessages {

  case class JobServerSupervisorActor_getJars()

  case class JobServerSupervisorActor_getJobs()

  case class JobServerSupervisorActor_getContexts()

  case class JobServerSupervisorActor_getJob(jobId: String)

  case class JobServerSupervisorActor_getJobConfig(jobId: String)

  case class JobServerSupervisorActor_deleteContext(contextId: String)

  case class JobServerSupervisorActor_deleteJob(jobId: String)

  case class JobServerSupervisorActor_uploadJars(files: Seq[File])

  case class JobServerSupervisorActor_uploadPolicy(jobName: String,
                                                   classPath: String,
                                                   policy: String,
                                                   context: Option[String])

  case class JobServerSupervisorActor_createContext(contextName: String,
                                                   cpuCores: String,
                                                   memory: String)

  case class JobServerSupervisorActor_response_getJars(jarResult: Try[JValue])

  case class JobServerSupervisorActor_response_getJobs(jobsResult: Try[JValue])

  case class JobServerSupervisorActor_response_getContexts(contextsResult: Try[JValue])

  case class JobServerSupervisorActor_response_getJob(jobInfoResult: Try[JValue])

  case class JobServerSupervisorActor_response_getJobConfig(jobInfoResult: Try[JValue])

  case class JobServerSupervisorActor_response_deleteContext(deleteContextResult: Try[JValue])

  case class JobServerSupervisorActor_response_deleteJob(deleteJobResult: Try[JValue])

  case class JobServerSupervisorActor_response_uploadJars(uploadJarResult: Try[String])

  case class JobServerSupervisorActor_response_uploadPolicy(uploadPolicyResult: Try[JValue])

  case class JobServerSupervisorActor_response_createContext(createContextResult: Try[JValue])
}
