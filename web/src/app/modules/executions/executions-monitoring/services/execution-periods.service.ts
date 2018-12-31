/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import * as moment from 'moment';
import { ExecutionService } from 'app/services/api/execution.service';
import { take, map } from 'rxjs/operators';
import { Engine } from '@models/enums';
import { Observable } from 'rxjs';

@Injectable()
export class ExecutionPeriodsService {

  constructor(
    private _executionService: ExecutionService
  ) { }

  getExecutionPeriodData(period: string): Observable<any> {
    switch (period) {
      case 'DAY':
        return this._getDayPeriod();
      case 'WEEK':
        return this._getWeekPeriod();
      case 'MONTH':
        return this._getMonthPeriod();
    }
  }

  private _getDayPeriod(): Observable<any> {
    const currentHour = new Date().getHours();
    const times = [];
    for (let i = 0; i < 24; i++) {
      const h = currentHour + i + 1;
      times.push(h > 23 ? h - 24 : h);
    }
    return this._executionService.getExecutionsByDate(
      'HOUR', moment().subtract(1, 'days').minutes(0).seconds(0).format('YYYY-MM-DDTHH:mm:ss') + 'Z',
      moment().minutes(0).seconds(0).format('YYYY-MM-DDTHH:mm:ss') + 'Z')
      .pipe(map((res: any) => {
        const engines = [Engine.Batch, Engine.Streaming];
        const [batchData, streamingData] = engines.map(engine => res.executionsSummaryByDate
          .filter(data => data.executionEngine === engine).map(data => {
            data.date = moment(data.date).valueOf();
            return data;
          }));
        const initialValues = new Array(times.length).fill(0);
        const batchTotal = times.map(time => batchData[time] || 0);
        const streamingTotal = times.map(time => streamingData[time] || 0);

        return {
          period: 'DAY',
          times: times.map(time => time.toString()),
          batchTotal: {
            data: batchTotal,
            label: 'Batch'
          },
          streamingTotal: {
            data: streamingTotal,
            label: 'Streaming'
          },
        };
      }));
  }

  private _getWeekPeriod(): Observable<any> {
    return this._executionService.getExecutionsByDate('HOUR', moment().subtract(1, 'days').minutes(0).seconds(0).format('YYYY-MM-DDTHH:mm:ss') + 'Z', moment().minutes(0).seconds(0).format('YYYY-MM-DDTHH:mm:ss') + 'Z');
  }

  private _getMonthPeriod(): Observable<any> {
    return this._executionService.getExecutionsByDate('HOUR', moment().subtract(1, 'days').minutes(0).seconds(0).format('YYYY-MM-DDTHH:mm:ss') + 'Z', moment().minutes(0).seconds(0).format('YYYY-MM-DDTHH:mm:ss') + 'Z');
  }
}
