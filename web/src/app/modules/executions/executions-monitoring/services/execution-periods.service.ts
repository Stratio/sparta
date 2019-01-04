/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import * as moment from 'moment';
import { ExecutionService } from 'app/services/api/execution.service';
import { take, map, filter, tap } from 'rxjs/operators';
import { Engine } from '@models/enums';
import { Observable } from 'rxjs';
import { AnyFn } from '@ngrx/store/src/selector';

@Injectable()
export class ExecutionPeriodsService {

  constructor(
    private _executionService: ExecutionService
  ) { }

  getExecutionPeriodData(period: string): Observable<any> {

    return this._getPeriod(period)
    .pipe(map((res: any) => res.executionsSummaryByDate))
    .pipe(map((executions: any) => this._disjoinEjecutions(executions, period)))
    .pipe(map((data: any) => {
      const periodArray: Array<any> = this._getPeriodArray(period);
      const batchTotal = this._getEjecutionsData(periodArray.map(periodsItem => periodsItem.data), data.batchData);
      const streamingTotal = this._getEjecutionsData(periodArray.map(periodsItem => periodsItem.data), data.streamingData);

      return {
        period,
        times: periodArray.map(time => time.label),
        batchTotal: {
          data: batchTotal,
          label: 'Batch'
        },
        streamingTotal: {
          data: streamingTotal,
          label: 'Streaming'
        }
      };
    }));
  }

  private _getPeriodArray(period: string): Array<Object> {
    const times: Array<Object> = [];
    const now: moment.Moment = moment();
    const granularitys: Object = Object.freeze({
      'DAY': {
        singular: 'hour',
        plural: 'hours',
        length: 23,
        getMethod: 'hour'
      },
      'WEEK': {
        singular: 'day',
        plural: 'days',
        length: 6,
        getMethod: 'isoWeekday'
      },
      'MONTH': {
        singular: 'day',
        plural: 'days',
        length: 30,
        getMethod: 'date'
      }
    });
    const granularity = granularitys[period];
    const past: moment.Moment = now.clone().subtract(granularity.length, granularity.plural);

    while (now.diff(past, granularity.plural) > 0) {
      times.push({
        label: period === 'WEEK' ? past.format('dddd') : past[granularity.getMethod]().toString(),
        data: past[granularity.getMethod]()
      });
      past.add(1, granularity.singular);
    }
    times.push({
      label: period === 'WEEK' ? past.format('dddd') : past[granularity.getMethod]().toString(),
      data: past[granularity.getMethod]()
    });

    return times;
  }

  private _disjoinEjecutions(executions: Array<any>, period: string): Object {
    const engines = [Engine.Batch, Engine.Streaming];
    const [batchData, streamingData] = engines.map(
      engine => executions
        .filter(data => data.executionEngine === engine)
        .map(data => {
          data.period = period === 'DAY'
            ? moment(data.date).hour()
            : period === 'WEEK'
              ? moment(data.date).day()
              : moment(data.date).date();
          return data;
        })
    );
    return {
      batchData,
      streamingData
    };
  }

  private _getPeriod(period): Observable<Object> {
    const momentPeriods: Object = {
      'DAY': {
        substractPeriod: 'days',
        requestPeriod: 'HOUR',
        daysLeft: 1
      },
      'WEEK': {
        substractPeriod: 'days',
        requestPeriod: 'DAY',
        daysLeft: 7
      },
      'MONTH': {
        substractPeriod: 'days',
        requestPeriod: 'DAY',
        daysLeft: 30
      }
    };
    const periodData = momentPeriods[period];

    return this._executionService.getExecutionsByDate(
      periodData.requestPeriod,
      moment().subtract(periodData.daysLeft, periodData.substractPeriod).minutes(0).seconds(0).format('YYYY-MM-DDTHH:mm:ss') + 'Z',
      moment().minutes(0).seconds(0).format('YYYY-MM-DDTHH:mm:ss') + 'Z'
    );
  }

  private _getEjecutionsData(times, executionsData): Array<number> {
    const initialEjecutions = Array.from(times, time => Object({
      executions: 0,
      period: time
    }));

    executionsData.forEach(execution => {
      const initialExecutionsFiltered: Array<Object> = initialEjecutions.filter(batchValue => batchValue.period === execution.period);
      const initialExecution: any = initialExecutionsFiltered && initialExecutionsFiltered.length
        ? initialExecutionsFiltered[0]
        : { executions: 0 };
        initialExecution.executions = execution.total;
    });

    return initialEjecutions.map(data => data.executions);
  }

}
