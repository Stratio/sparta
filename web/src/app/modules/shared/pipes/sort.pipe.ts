///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

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
