/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.marathon.builder

import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant.BaseZkPath
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.marathon._
import com.stratio.sparta.serving.core.marathon.factory.MarathonApplicationFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.typesafe.config.Config
import org.json4s.jackson.Serialization.write
import MarathonApplicationFactory._

import scala.util.{Properties, Try}

object StratioBuilderImplicits {

  lazy val useDynamicAuthentication: Boolean = Try(scala.util.Properties.envOrElse(DynamicAuthEnv, "false").toBoolean).getOrElse(false)
  lazy val marathonConfig: Config = SpartaConfig.getMarathonConfig().get

  implicit class Builder(marathonApplication: MarathonApplication) extends SpartaSerializer {

    def withStratioVolumes: MarathonApplication = {
      val commonVolumes: Seq[MarathonVolume] = {
        if (Try(marathonConfig.getString("docker.includeCommonVolumes").toBoolean).getOrElse(DefaultIncludeCommonVolumes))
          Seq(MarathonVolume(ResolvConfigFile, ResolvConfigFile, "RO"), MarathonVolume(Krb5ConfFile, Krb5ConfFile, "RO"))
        else Seq.empty[MarathonVolume]
      }

      val javaCertificatesVolume: Seq[MarathonVolume] = {
        if (!Try(marathonConfig.getString("docker.includeCertVolumes").toBoolean).getOrElse(DefaultIncludeCertVolumes) ||
          (envOrNone(VaultEnableEnv).isDefined && envOrNone(VaultHostsEnv).isDefined &&
            envOrNone(VaultTokenEnv).isDefined))
          Seq.empty[MarathonVolume]
        else Seq(
          MarathonVolume(ContainerCertificatePath, HostCertificatePath, "RO"),
          MarathonVolume(ContainerJavaCertificatePath, HostJavaCertificatePath, "RO")
        )
      }

      val defaultContainerVolumes: Seq[MarathonVolume] = {
        envOrNone(MesosNativeJavaLibraryEnv) match {
          case Some(_) =>
            Seq.empty
          case None =>
            Seq(MarathonVolume(HostMesosNativeLibPath, mesosphereLibPath, "RO"),
              MarathonVolume(HostMesosNativePackagesPath, mesospherePackagesPath, "RO"))
        }
      } ++ commonVolumes ++ javaCertificatesVolume

      marathonApplication.copy(
        container = marathonApplication.container.copy(
          volumes = Option(marathonApplication.container.volumes.getOrElse(Seq.empty[MarathonVolume]) ++ defaultContainerVolumes)
        )
      )
    }

    def withIpAddress: MarathonApplication = {
      marathonApplication.copy(
        ipAddress = if (isCalicoEnabled) {
          Option(IpAddress(networkName = envOrNone(CalicoNetworkEnv)))
        } else None
      )
    }

    def withDynamicAuthenticationEnv: MarathonApplication = {
      val (dynamicAuthEnv, defaultSecrets) = {
        val appRoleName = envOrNone(AppRoleNameEnv)
        if (useDynamicAuthentication && appRoleName.isDefined) {
          (Map(AppRoleEnv -> write(Map("secret" -> "role"))), Map("role" -> Map("source" -> appRoleName.get)))
        } else (Map.empty[String, String], Map.empty[String, Map[String, String]])
      }

      marathonApplication.copy(
        env = Option(marathonApplication.env.getOrElse(Map.empty[String, String] ++ dynamicAuthEnv)),
        secrets = defaultSecrets
      )
    }

    def withStratioDefaultLabels: MarathonApplication = {
      val defaultLabels = envOrNone(DcosServiceCompanyLabelPrefix).fold(Map.empty[String, String]) {
        companyPrefix =>
          sys.env.filterKeys { key =>
            key.contains(MarathonLabelPrefixEnv) && key.contains(companyPrefix)
          }.map { case (key, value) =>
            key.replaceAll(MarathonLabelPrefixEnv, "") -> value
          }
      }

      marathonApplication.copy(
        labels = marathonApplication.labels ++ defaultLabels
      )
    }

    def withAdminRouterLabels: MarathonApplication = {
      val dcosServiceLabels = Map(
        "DCOS_SERVICE_PORT_INDEX" -> "0",
        "DCOS_SERVICE_SCHEME" -> "http"
      )

      marathonApplication.copy(
        labels = marathonApplication.labels ++ dcosServiceLabels
      )
    }

    def withPortMappings: MarathonApplication = {
      val portMappings: Option[Seq[DockerPortMapping]] = {
        if (isCalicoEnabled)
          Option(
            Seq(
              DockerPortMapping(
                hostPort = DefaultRandomPort,
                containerPort = DefaultSparkUIRedirectionPort,
                servicePort = Option(DefaultRandomPort),
                protocol = "tcp",
                name = Option(MarathonApplicationFactory.SparkUIRedirectionsPortName)

              ),
              DockerPortMapping(
                hostPort = DefaultRandomPort,
                containerPort = DefaultMetricsMarathonDriverPort,
                servicePort = Option(DefaultRandomPort),
                protocol = "tcp",
                name = Option("metrics")
              ),
              DockerPortMapping(
                hostPort = DefaultRandomPort,
                containerPort = DefaultJmxMetricsMarathonDriverPort,
                servicePort = Option(DefaultRandomPort),
                protocol = "tcp",
                name = Option("jmx")
              ),
              DockerPortMapping(
                hostPort = DefaultRandomPort,
                containerPort = SparkUIPort,
                servicePort = Option(SparkUIServicePort),
                protocol = "tcp",
                name = Option(SparkUIPortName)
              )
            ) ++
              marathonApplication.container.docker.portMappings.getOrElse(Seq.empty)
          )
        else marathonApplication.container.docker.portMappings
      }

      marathonApplication.copy(
          container = marathonApplication.container.copy(
          docker = marathonApplication.container.docker.copy(
            portMappings = portMappings
          )
        )
      )

    }

    def withNetworkType: MarathonApplication =
      marathonApplication.copy(
        container = marathonApplication.container.copy(
          docker = marathonApplication.container.docker.copy(
            network = if (isCalicoEnabled) "USER" else marathonApplication.container.docker.network
          )
        )
      )

    def withPortDefinitions: MarathonApplication =
      marathonApplication.copy(
        portDefinitions = if (isCalicoEnabled) None else marathonApplication.portDefinitions
      )

    def withCompanyBillingLabels: MarathonApplication =
      marathonApplication.copy(
        labels = marathonApplication.labels ++ companyBillingLabels
      )

    def addMarathonJarEnv: MarathonApplication = {
      val marathonJar: String = Try(marathonConfig.getString("jar")).toOption.getOrElse(AppConstant.DefaultMarathonDriverURI)

      marathonApplication.copy(
        env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ Map(AppJarEnv -> marathonJar))
      )
    }

    def addVaultTokenEnv: MarathonApplication = {
      val vaultToken: Option[String] = if (!useDynamicAuthentication) Properties.envOrNone(VaultTokenEnv) else None

      vaultToken.foldLeft(marathonApplication) ((ma,token) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ Map(VaultTokenEnv -> token))
        )
      })
    }

    def addMesosNativeJavaLibraryEnv: MarathonApplication = {
      val mesosNativeJavaLibraryMaybe = Properties.envOrNone(MesosNativeJavaLibraryEnv).orElse(Option(HostMesosNativeLib))
      mesosNativeJavaLibraryMaybe.foldLeft(marathonApplication) ((ma, mesosNativeJavaLibrary) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ Map(MesosNativeJavaLibraryEnv -> mesosNativeJavaLibrary))
        )
      })
    }

    def addLdLibraryEnv: MarathonApplication = {
      val ldNativeLibraryMaybe = Properties.envOrNone(MesosNativeJavaLibraryEnv) match {
        case Some(_) => None
        case None => Option(HostMesosLib)
      }
      ldNativeLibraryMaybe.foldLeft(marathonApplication) ((ma, ldNativeLibrary) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ Map(LdLibraryEnv -> ldNativeLibrary))
        )
      })
    }


    def addDynamicAuthEnv: MarathonApplication = {
      val dynamicAuthMaybe = Properties.envOrNone(DynamicAuthEnv)
      dynamicAuthMaybe.foldLeft(marathonApplication) ((ma, dynamicAuth) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ Map(DynamicAuthEnv -> dynamicAuth))
        )
      })
    }

    def addSpartaFileEncodingEnv: MarathonApplication = {
      val spartaFileEncodingMaybe = Some(Properties.envOrElse(SpartaFileEncoding, DefaultFileEncodingSystemProperty))
      spartaFileEncodingMaybe.foldLeft(marathonApplication) ((ma, spartaFileEncoding) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ Map(SpartaFileEncoding -> spartaFileEncoding))
        )
      })
    }

    def addSparkHomeEnv: MarathonApplication = {
      val sparkHomeEnvMaybe = Properties.envOrNone(SparkHomeEnv)
      sparkHomeEnvMaybe.foldLeft(marathonApplication) ((ma, sparkHomeEnv) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ Map(SparkHomeEnv -> sparkHomeEnv))
        )
      })
    }

    def addDatastoreCaNameEnv: MarathonApplication = {
      val datastoreCaNameMaybe = Properties.envOrSome(DatastoreCaNameEnv, Option("ca"))
      datastoreCaNameMaybe.foldLeft(marathonApplication) ((ma, datastoreCaName) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ Map(DatastoreCaNameEnv -> datastoreCaName))
        )
      })
    }

    def addSpartaSecretFolderEnv: MarathonApplication = {
      val spartaSecretFolderMaybe = Properties.envOrNone(SpartaSecretFolderEnv)
      spartaSecretFolderMaybe.foldLeft(marathonApplication) ((ma, spartaSecretFolder) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ Map(SpartaSecretFolderEnv -> spartaSecretFolder))
        )
      })
    }

    def addSpartaZookeeperPathEnv: MarathonApplication = {
      val spartaZookeeperPathMaybe =Option(BaseZkPath)
      spartaZookeeperPathMaybe.foldLeft(marathonApplication) ((ma, spartaZookeeperPath) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String,String]) ++ Map(SpartaZookeeperPathEnv -> spartaZookeeperPath))
        )
      })
    }

    protected[builder] def envOrNone(environmentVariable: String): Option[String] = {
      Properties.envOrNone(environmentVariable)
    }

    protected[builder] def addEnv(newEnvs: Map[String, String]): MarathonApplication =
      marathonApplication.copy(
        env = Option(marathonApplication.env.getOrElse(Map.empty[String, String]) ++ newEnvs)
      )
  }

  protected[builder] def mesosphereLibPath: String =
    Try(marathonConfig.getString("mesosphere.lib")).toOption.getOrElse(HostMesosNativeLibPath)

  protected[builder] def mesospherePackagesPath: String =
    Try(marathonConfig.getString("mesosphere.packages")).toOption.getOrElse(HostMesosNativePackagesPath)

  private def companyBillingLabels: Map[String, String] = {
    Properties.envOrNone(DcosServiceCompanyLabelPrefix).fold(Map.empty[String, String]) { companyPrefix =>
      sys.env.filterKeys { key =>
        key.contains(MarathonLabelPrefixEnv) && key.contains(companyPrefix)
      }.map { case (key, value) =>
        key.replaceAll(MarathonLabelPrefixEnv, "") -> value
      }
    }
  }

  lazy protected[builder] val isCalicoEnabled: Boolean = {
    import com.stratio.sparta.core.properties.ValidatingPropertyMap._
    val calicoEnabled = Properties.envOrNone(CalicoEnableEnv).notBlank
    val calicoNetwork = Properties.envOrNone(CalicoNetworkEnv).notBlank
    calicoEnabled.isDefined && calicoEnabled.get.equals("true") && calicoNetwork.isDefined
  }
}
