/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
const typeCache: { [label: string]: boolean } = {};

const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

export function type<T>(label: T | ''): T {
  if (typeCache[<string>label]) {
    throw new Error(`Action type "${label}" is not unique"`);
  }

  typeCache[<string>label] = true;

  return <T>label;
}

export function generateJsonFile(name: string, content: any) {
  const data = 'text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(content));
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
    let avalue = getOrderPropertyValue(a, orderProperty) || '';
    let bvalue = getOrderPropertyValue(b, orderProperty) || '';

    avalue = avalue && typeof avalue === 'string' ? avalue.toString().toUpperCase() : avalue;
    bvalue = bvalue && typeof bvalue === 'string'  ? bvalue.toString().toUpperCase() : bvalue;

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
        if(!val[properties[i]]) {
          return null;
        }
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

export function formatDate(stringDate: string, hours = true, complete = false) {
  if(!stringDate) {
    return '';
  }
  try {
    const date: Date = new Date(stringDate);
    const todaysDate = new Date();
    // call setHours to take the time out of the comparison
    if (!complete && date.setHours(0, 0, 0, 0) === todaysDate.setHours(0, 0, 0, 0)) {
      // Date equals today's date
      const dateToday: Date = new Date(stringDate);
      return dateToday.getHours() + ':' + ('0' + dateToday.getMinutes()).slice(-2);

    } else {
      const date: Date = new Date(stringDate);
      const month: any = date.getMonth();
      return hours ?  date.getDate() + ' ' + monthNames[month] + ' ' + date.getFullYear() + ' - ' + date.getHours() + ':' + ('0' + date.getMinutes()).slice(-2):
        date.getDate() + ' ' + 'Jan' + ' ' + date.getFullYear();

    }
  } catch (error) {
    return '';
  }
}

/* Starting | Running | Stopped | Failed | NotStarted*/
export function getFilterStatus(status: string) {
  switch (status) {
    case 'Launched':
      return 'Starting';
    case 'Starting':
      return status;
    case 'Started':
      return 'Running';
    case 'Running':
      return status;
    case 'Stopping':
      return 'Stopped';
    case 'StoppingByUser':
      return 'Stopped';
    case 'Stopped':
      return status;
    case 'StoppedByUser':
      return 'Stopped';
    case 'Finished':
      return 'Stopped';
    case 'Killed':
      return 'Stopped';
    case 'NotStarted':
      return 'Starting';
    case 'Uploaded':
      return 'Starting';
    case 'Created':
      return 'Stopped';
    case 'Failed':
      return 'Failed';
    default:
      return '';
  }
};

export function isWorkflowRunning(policyStatus: string) {
    const status = policyStatus ? policyStatus.toLowerCase() : '';
    return status.length && (status === 'starting'
        || status === 'started'
        || status === 'uploaded'
        || status === 'launched'
        || status === 'notstarted');
}

export function reduceReducers(...reducers) {
    return (previous, current) =>
        reducers.reduce(
            (p, r) => r(p, current),
            previous
        );
}

export function mergeNoDuplicatedArrays(array1: any[], array2: any[], keyName: string, valueName: string): any[] {
   let result = [];
   if (array1 && array2) {
      result = [...array1];
      let currentPropertyNames = result.map(item => item[keyName]);
      array2.forEach(item => {
         const propertyPosition = currentPropertyNames.indexOf(item[keyName]);
         if (propertyPosition === -1) {
            result.push(item);
            currentPropertyNames.push(item[keyName]);
         } else if (!result[propertyPosition][valueName]) {
            result[propertyPosition] = item;
         }
      });
   }
   return result;
}

/**
 * Copy content into the clipboard
 *
 * @param content [String] String to copy into the clipboard
 */
export function copyIntoClipboard(content: string) {
    try {
      const copyText = document.createElement('textarea');
      copyText.value = content;
      copyText.style.opacity = '0';
      const el = document.body.appendChild(copyText);
      el.select();
      document.execCommand('copy');
      el.remove();
    } catch (e) { }
  }
