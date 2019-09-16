/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration.hydra_pegaso

import com.stratio.sparta.serving.core.models.workflow.migration.WorkflowHydraPegaso
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection
import slick.jdbc.PostgresProfile

import scala.concurrent.Future

//scalastyle:off
class WorkflowHydraPegasoPostgresDao extends WorkflowHydraPegasoDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  import profile.api._

  def findAllWorkflows(): Future[Seq[WorkflowHydraPegaso]] =
    db.run(table.filter(t =>
      (t.versionSparta like s"%2.5%") ||
        (t.versionSparta like s"%2.6%") ||
        (t.versionSparta like s"%2.7%") ||
        (t.versionSparta like s"%2.8%") ||
        (t.versionSparta like s"%2.9%") ||
        (t.versionSparta like s"%2.10%")
    ).result
    )
}


