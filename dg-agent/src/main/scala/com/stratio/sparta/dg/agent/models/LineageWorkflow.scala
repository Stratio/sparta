/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.dg.agent.models

case class LineageWorkflow(
                            id: Int,
                            name: String,
                            description: String,
                            tenant: Option[String],
                            properties: Map[String,String],
                            transactionId: String,
                            actorType : String,
                            jobType: String,
                            statusCode: String,
                            version: String,
                            listActorMetaData: List[ActorMetadata]
                          )

case class ActorMetadata(
                          id: Option[Int] = None,
                          `type`: String,
                          metaDataPath: String,
                          dataStoreType: String,
                          tenant: Option[String],
                          properties: Map[String,String]
                        )