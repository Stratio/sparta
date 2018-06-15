/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.security

case class Resource(resourceType: ResourceType, name: String)

sealed trait ResourceType{
  def name(): String
}

case object BackupResource extends ResourceType{
  override def name(): String = "Backup"
}

case object CatalogResource extends ResourceType{
  override def name(): String = "Catalog"
}

case object ConfigurationResource extends ResourceType{
  override def name(): String = "Configuration"
}

case object EnvironmentResource extends ResourceType{
  override def name(): String = "Environment"
}

case object WorkflowGroupResource extends ResourceType{
  override def name(): String = "Workflow Group"
}

case object GroupsResource extends ResourceType{
  override def name(): String = "Groups"
}

case object PluginResource extends ResourceType{
  override def name(): String = "Plugin"
}

case object TemplateResource extends ResourceType{
  override def name(): String = "Template"
}

case object WorkflowsResource extends ResourceType{
  override def name(): String = "Workflows"
}

case object WorkflowDetailResource extends ResourceType{
  override def name(): String = "Workflow Detail"
}

case object HistoryResource extends ResourceType{
  override def name(): String = "History"
}