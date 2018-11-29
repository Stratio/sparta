/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter'
})
export class FilterPipe implements PipeTransform {
  transform(items: any, term: any): any {
    if (term) {
      return items.filter((item: any) => {
        return Object.keys(item).some(
          k => {
            if (item[k] != null && item[k] != undefined && typeof item[k] == 'string')
              return item[k].includes(term.toLowerCase());
          }
        );
      });
    }
    return items;
  }
}
