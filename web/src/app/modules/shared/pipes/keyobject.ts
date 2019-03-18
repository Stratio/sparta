/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable, Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'keyobject'
})
@Injectable()
export class Keyobject implements PipeTransform {

  transform(value: any, args: string[]): any {
    const keys = [];
    for (const key in value) {
      keys.push({ key: key, value: value[key] });
    }
    return keys;
  }
}
