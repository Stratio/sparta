/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.json

import com.jayway.jsonpath.{Configuration, JsonPath, ReadContext}

class JsonPathExtractor(jsonDoc: String, isLeafToNull: Boolean) {

  val conf = {
    if (isLeafToNull)
      Configuration.defaultConfiguration().addOptions(com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL)
    else Configuration.defaultConfiguration()
  }

  private val ctx: ReadContext = JsonPath.using(conf).parse(jsonDoc)

  def query(query: String): Any = ctx.read(query)

}
