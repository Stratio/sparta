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

const typeCache: { [label: string]: boolean } = {};
export function type<T>(label: T | ''): T {
  if (typeCache[<string>label]) {
    throw new Error(`Action type "${label}" is not unique"`);
  }

  typeCache[<string>label] = true;

  return <T>label;
}

export function generateJsonFile(name: string, content: any) {
  const data = 'text/json;charset=utf-8,' + JSON.stringify(content);
  const a = document.createElement('a');
  a.href = 'data:' + data;
  a.download = name + '.json';
  document.body.appendChild(a);
  a.click();
  a.remove();
}


// order an array of objects/values by property
export function orderBy(array: Array<any>, orderProperty: string, order: boolean): Array<any> {
  const orderAux = order ? 1 : -1;
  array.sort((a: any, b: any) => {
    let avalue = getOrderPropertyValue(a, orderProperty);
    let bvalue = getOrderPropertyValue(b, orderProperty);

    avalue = avalue ? avalue.toString().toUpperCase() : '';
    bvalue = bvalue ? bvalue.toString().toUpperCase() : '';

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

function getOrderPropertyValue(value: any, orderProperty: string): any {
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

