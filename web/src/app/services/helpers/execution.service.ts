/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { formatDate } from '@utils';

@Injectable()
export class ExecutionHelperService {

  public normalizeExecution(execution) {
    const { id, statuses, marathonExecution, localExecution, genericDataExecution: { endDate, startDate, workflow: { name, group, version, executionEngine }, executionContext: { paramsLists: context } } } = execution;
    let startDateMillis = 0;
    let startDateFormatted = '-';
    try {
      startDateMillis = new Date(startDate).getTime();
      startDateFormatted = formatDate(startDate);
    } catch (error) { }
    let endDateMillis = 0;
    let endDateFormatted = '-';
    try {
      endDateMillis = new Date(endDate).getTime();
      endDateFormatted = formatDate(endDate);
    } catch (error) { }
    return {
      id,
      name,
      version,
      sparkURI: localExecution ? localExecution.sparkURI : marathonExecution && marathonExecution.sparkURI || '',
      endDate,
      endDateMillis,
      endDateFormatted,
      startDate,
      startDateMillis,
      startDateFormatted,
      group,
      executionEngine,
      context: context.join(),
      status: statuses.length && statuses[0].state
    };
  }
}
