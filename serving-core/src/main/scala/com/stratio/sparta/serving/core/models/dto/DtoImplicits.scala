/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.dto

/**
  * Base Trait for all dto´s classes. This trait is used by the generic list implicit conversion
  */
trait Dto

object DtoImplicits {

  /**
    * Generic Implicit transformation from Seq[G] to Seq[T] (the related implicit transformation from G to T, are
    * defined in each G type Actor class
    */
  implicit def toDtoList[G, T <: Dto](elem: Seq[G])(implicit converter: G => T): Seq[T] = {
    for {
      e <- elem
    } yield (converter(e))
  }
}
