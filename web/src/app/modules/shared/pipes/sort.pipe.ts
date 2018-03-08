/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'orderBy',
  pure: false
})
export class OrderByPipe implements PipeTransform {

  transform(array: Array<any>, orderProperty: string, order: boolean): Array<any> {
     const orderAux = order ? 1 : -1;
    array.sort((a: any, b: any) => {
      const avalue = this.getOrderProperyValue(a, orderProperty);
      const bvalue = this.getOrderProperyValue(b, orderProperty);
      if (avalue < bvalue) {
        return -(orderAux);
      } else if (avalue > bvalue) {
        return orderAux;
      } else {
        return 0;
      }
    });
    return array;
  }

  getOrderProperyValue(value: any, orderProperty: string): any {
    if (orderProperty) {
      const properties: Array<any> = orderProperty.split('.');
      if (properties.length > 1) {
        let val = value;
        for (let i = 0; i < properties.length; i++) {
          val = val[properties[i]];
        }
        return val;
      } else {
        return value[orderProperty];
      }
    } else {
      return '';
    }
  }
}
