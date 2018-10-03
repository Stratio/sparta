/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.custom

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.utils.ClasspathUtils
import com.stratio.sparta.sdk.lite.xd.common.LiteCustomXDOutput
import org.apache.spark.sql.crossdata.XDSession

import scala.util.Try

class CustomLiteXDOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends CustomLiteOutputCommon[LiteCustomXDOutput](name, xDSession, properties) {

  // TODO refactor
  override def customStep: Try[LiteCustomXDOutput] = Try {
    val customClassProperty = customClassType.getOrElse(throw new Exception("The class property is mandatory"))
    val classpathUtils = new ClasspathUtils
    val (customClass, customClassAndPackage) = classpathUtils.getCustomClassAndPackage(customClassProperty)
    val properties = propertiesWithCustom.mapValues(_.toString)

    classpathUtils.tryToInstantiate[LiteCustomXDOutput](
      classAndPackage = customClass,
      block = (c) => {
        val constructor = c.getDeclaredConstructor(
          classOf[XDSession],
          classOf[Map[String, String]]
        )
        val instance = constructor.newInstance(xDSession, properties)

        instance.asInstanceOf[LiteCustomXDOutput]
      },
      inputClazzMap = Map(customClass -> customClassAndPackage)
    )
  }

}


