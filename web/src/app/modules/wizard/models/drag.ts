/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export interface ZoomTransform {
   x: number;
   y: number;
   k: number;
}

export interface CreationData {
   active: boolean;
   data: any;
}

export interface NodeConnector {
    x1: number;
    x2: number;
    y1: number;
    y2: number;
}
