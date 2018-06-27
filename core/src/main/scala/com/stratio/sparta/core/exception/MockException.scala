/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.exception

import scala.util.control.NoStackTrace

/**
 * This class id used to create custom exceptions that will be mostly used in tests
 * with the particularity that it has not trace.
 */
class MockException extends NoStackTrace {

}
