/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.gosec.dyplon.plugins.sparta

import com.stratio.gosec.api.Systems._
import com.stratio.gosec.api.announcement.repository.AnnouncementRepositoryComponentImpl
import com.stratio.gosec.api.announcement.service.AnnouncementServiceComponentImpl
import com.stratio.gosec.api.config.ConfigComponentImpl
import com.stratio.gosec.api.enrollment.repository.EnrollmentWatcherRepositoryComponentImpl
import com.stratio.gosec.api.utils.repository.zookeeper.ZkRepositoryComponentImpl
import com.stratio.gosec.dyplon.announcement.repository.ZkWatcherRepositoryComponentImpl
import com.stratio.gosec.dyplon.audit.AbstractLifeCycle
import com.stratio.gosec.dyplon.core.Lifecycle
import com.stratio.gosec.dyplon.core.ZkRegisterCheckerComponentImpl
import com.stratio.gosec.dyplon.model.PluginInstance

object SpartaSystem{

  case class SpartaLifeCycleSystem(plugin: PluginInstance) extends Lifecycle(plugin)
    with AbstractLifeCycle
    with AnnouncementServiceComponentImpl
    with EnrollmentWatcherRepositoryComponentImpl
    with AnnouncementRepositoryComponentImpl
    with ZkRegisterCheckerComponentImpl
    with ZkRepositoryComponentImpl
    with ConfigComponentImpl
    with ZkWatcherRepositoryComponentImpl
    with DefaultPolicyService
    with DefaultPluginService
    with DefaultUserService
    with DefaultGroupService
    with DefaultInstanceService
}