/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.governance

case class GovernanceQualityRule(content: Seq[Content],
                                 pageable: Pageable,
                                 totalElements: Long,
                                 totalPages: Long,
                                 last: Boolean,
                                 first: Boolean,
                                 sort: Sort,
                                 numberOfElements: Long,
                                 size: Long,
                                 number: Long)

case class Sort(sorted: Boolean,
                unsorted: Boolean)

case class Content(id: Long,
                   metadataPath: String,
                   name: String,
                   description: String,
                   `type`: String,
                   catalogAttributeType: String,
                   parameters: Parameters,
                   query: Option[String],
                   active: Boolean,
                   resultUnit: NameValue,
                   resultOperation: String,
                   resultOperationType: String,
                   resultAction: ResultAction,
                   resultExecute: ResultExecute,
                   tenant: String,
                   createdAt: String,
                   modifiedAt: String
                  )

case class Cond(order: Int,
                attribute: String,
                operation: String,
                `type`: Option[String],
                param: Option[Seq[NameValue]]
                )

case class Parameters(catalogAttributeType: Option[String],
                      filter: Option[Filter],
                      advanced: Option[Advanced],
                      table: Option[Table]
                     )

case class Table(`type`: String)

case class NameValue(name: String, value: String)

case class TableParams(operation: String, params: Seq[NameValue])

case class Filter(order: Double,
                  `type`: String,
                  cond: Seq[Cond]
                 )

case class ResultAction(path: Option[String],
                        `type`: String)

case class ResultExecute(`type`: String,
                         config: Option[Configuration]
                        )

case class Pageable(
                     sort: Sort,
                     pageSize: Long,
                     pageNumber: Long,
                     offset: Long,
                     unpaged: Boolean,
                     paged: Boolean)

case class Advanced(query: String,
                    resources: Seq[Resources],
                    queryReference: String
                   )

case class Resources(
                    id: Long,
                    resource: String,
                    metadatapath: String,
                    `type`: String)

case class Configuration(scheduling: Seq[Scheduling],
                          executionOptions: ExecutionOptions)

case class Scheduling(
                     initialization: Long,
                     period: Option[Long]
                     )

case class ExecutionOptions(size: String)