/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.marathon

case class MarathonApplication(
                                id: String,
                                cpus: Double,
                                mem: Int,
                                instances: Option[Int] = None,
                                user: Option[String] = None,
                                args: Option[List[String]] = None,
                                env: Option[Map[String, String]] = None,
                                container: MarathonContainer,
                                cmd: Option[String] = None,
                                portDefinitions: Option[Seq[MarathonPortDefinition]] = None,
                                requirePorts: Option[Boolean] = None,
                                healthChecks: Option[Seq[MarathonHealthCheck]] = None,
                                labels: Map[String, String] = Map.empty[String, String],
                                ports: Option[Seq[Int]] = None,
                                constraints: Option[Seq[Seq[String]]] = None,
                                ipAddress: Option[IpAddress] = None,
                                secrets: Map[String, Map[String, String]] = Map.empty[String, Map[String, String]]
                              )

object MarathonApplication {

  val TcpValue = "tcp"
  val BridgeValue = "BRIDGE"
  val defaultMarathonApplication = MarathonApplication(
    id = "",
    cpus = 1,
    mem = 1024,
    instances = Option(1),
    user = None,
    args = None,
    env = None,
    container = MarathonContainer(
      docker = Docker(
        image = "",
        network = "HOST",
        portMappings = None,
        privileged = Option(false),
        parameters = None,
        forcePullImage = Option(false)
      ),
      `type` = "DOCKER",
      volumes = None
    ),
    cmd = None,
    portDefinitions = Option(Seq(
      MarathonPortDefinition(
        name = Option("sparkui"),
        port = 0,
        labels = None
      ),
      MarathonPortDefinition(
        name = Option("metrics"),
        port = 0,
        labels = None
      ),
      MarathonPortDefinition(
        name = Option("jmx"),
        port = 0,
        labels = None
      )
    )),
    requirePorts = None,
    healthChecks = Option(Seq(
      MarathonHealthCheck(
        protocol = "HTTP",
        path = Option("/environment"),
        portIndex = Option(0),
        command = None,
        gracePeriodSeconds = 240,
        intervalSeconds = 60,
        timeoutSeconds = 30,
        maxConsecutiveFailures = 3,
        ignoreHttp1xx = Option(false)
      ))),
    labels = Map(
      "DCOS_SERVICE_PORT_INDEX" -> "0",
      "DCOS_SERVICE_SCHEME" -> "http"
    ),
    ports = None,
    constraints = None,
    ipAddress = None,
    secrets = Map.empty[String, Map[String, String]]
  )
}

case class MarathonContainer(docker: Docker, `type`: String = "DOCKER", volumes: Option[Seq[MarathonVolume]] = None)

case class Docker(
                   image: String,
                   portMappings: Option[Seq[DockerPortMapping]] = None,
                   network: String = "HOST",
                   privileged: Option[Boolean] = None,
                   parameters: Option[Seq[DockerParameter]] = None,
                   forcePullImage: Option[Boolean] = None
                 )

case class DockerParameter(key: String, value: String)

case class MarathonVolume(containerPath: String, hostPath: String, mode: String)

case class DockerPortMapping(
                              hostPort: Int,
                              containerPort: Int,
                              servicePort: Option[Int] = None,
                              protocol: String = MarathonApplication.TcpValue,
                              labels: Option[Map[String, String]] = None,
                              name: Option[String] = None
                            )

case class MarathonPortDefinition(
                                   name: Option[String] = None,
                                   port: Int,
                                   protocol: String = MarathonApplication.TcpValue,
                                   labels: Option[Map[String, String]] = None
                                 )

case class MarathonHealthCheck(
                                protocol: String,
                                path: Option[String] = None,
                                portIndex: Option[Int] = None,
                                command: Option[MarathonHealthCheckCommand] = None,
                                gracePeriodSeconds: Int,
                                intervalSeconds: Int,
                                timeoutSeconds: Int,
                                maxConsecutiveFailures: Int,
                                ignoreHttp1xx: Option[Boolean] = None
                              )

case class MarathonHealthCheckCommand(value: String)

case class IpAddress(
                      networkName: Option[String] = None,
                      discovery: Option[DiscoveryInfo] = None,
                      groups: Option[Seq[String]] = None,
                      labels: Option[Map[String, String]] = None
                    )

case class DiscoveryInfo(ports: Seq[PortAddressDefinition])

case class PortAddressDefinition(
                                  number: Int,
                                  name: String,
                                  protocol: String
                                )

