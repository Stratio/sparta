/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import * as moment from 'moment';
import { ExecutionService } from 'app/services/api/execution.service';
import { map } from 'rxjs/operators';
import { Engine } from '@models/enums';
import { Observable } from 'rxjs';

export interface MomentPeriod {
  substractPeriod: string;
  requestPeriod: string;
  daysLeft: Number;
}

export interface MomentPeriods {
  DAY: MomentPeriod;
  WEEK: MomentPeriod;
  MONTH: MomentPeriod;
}

export interface Granularity {
  singular: string;
  plural: string;
  length: Number;
  getMethod: string;
}

export interface Granularities {
  DAY: Granularity;
  WEEK: Granularity;
  MONTH: Granularity;
}

export interface TotalExecutions {
  data: Array<number>;
  label: String;
}

export interface ExecutionChartData {
  period: string;
  times: Array<string>;
  batchTotal: TotalExecutions;
  streamingTotal: TotalExecutions;
}

export interface LabelChartInfo {
  label: string;
  data: number;
  moment: moment.Moment;
}

export interface ExecutionData {
  date: string;
  executionEngine: string;
  period: string;
  time: moment.Moment;
  total: number;
}

export interface ExecutionsData {
  batchData: Array<ExecutionData>;
  streamingData: Array<ExecutionData>;
}

@Injectable()
export class ExecutionPeriodsService {

  private _momentPeriods: MomentPeriods = Object.freeze({
    DAY: {
      substractPeriod: 'days',
      requestPeriod: 'HOUR',
      daysLeft: 1
    },
    WEEK: {
      substractPeriod: 'days',
      requestPeriod: 'DAY',
      daysLeft: 7
    },
    MONTH: {
      substractPeriod: 'days',
      requestPeriod: 'DAY',
      daysLeft: 30
    }
  });

  private _granularitys: Granularities = Object.freeze({
    DAY: {
      singular: 'hour',
      plural: 'hours',
      length: 23,
      getMethod: 'hour'
    },
    WEEK: {
      singular: 'day',
      plural: 'days',
      length: 6,
      getMethod: 'isoWeekday'
    },
    MONTH: {
      singular: 'day',
      plural: 'days',
      length: 30,
      getMethod: 'date'
    }
  });

  constructor(private _executionService: ExecutionService) {}

  /**
   * Main method returns the data with the number of the executions for the chart
   * @param period - type of period.
   */
  getExecutionPeriodData(period: string): Observable<ExecutionChartData> {
    return this._getPeriod(period)
      .pipe(
        map((res: any) => res.executionsSummaryByDate),
        map((executions: any) => this._disjoinEjecutions(executions, period)),
        map((data: any) => {
          const periodArray: Array<any> = this._getPeriodArray(period);
          const batchTotal = this._getEjecutionsData(
            periodArray.map(periodsItem => periodsItem.moment),
            data.batchData
          );
          const streamingTotal = this._getEjecutionsData(
            periodArray.map(periodsItem => periodsItem.moment),
            data.streamingData
          );

          const times = periodArray.map(time => time.label);
          return {
            period,
            times:
              period === 'DAY'
                ? times.map(val =>
                    val % 2 ? '' : `${val.padStart(2, '0')}:00`
                  )
                : times,
            batchTotal: {
              data: batchTotal,
              label: 'Batch'
            },
            streamingTotal: {
              data: streamingTotal,
              label: 'Streaming'
            }
          };
        })
      );
  }

  /**
   * Get an Array with information each moment in the chart.
   * @param period
   */
  private _getPeriodArray(period: string): Array<LabelChartInfo> {
    const times: Array<any> = [];
    const now: moment.Moment = moment();
    const granularity = this._granularitys[period];
    const past: moment.Moment = now
      .clone()
      .subtract(granularity.length, granularity.plural);

    while (now.diff(past, granularity.plural) > 0) {
      times.push({
        label:
          period === 'WEEK'
            ? past.format('dddd')
            : past[granularity.getMethod]().toString(),
        data: past[granularity.getMethod](),
        moment: moment(past)
      });
      past.add(1, granularity.singular);
    }

    times.push({
      label:
        period === 'WEEK'
          ? past.format('dddd')
          : past[granularity.getMethod]().toString(),
      data: past[granularity.getMethod](),
      moment: past
    });
    return times;
  }

  private _disjoinEjecutions(executions: Array<ExecutionData>, period: string): ExecutionsData {
    const engines = [Engine.Batch, Engine.Streaming];
    const [batchData, streamingData] = engines.map(engine =>
      executions
        .filter(data => data.executionEngine === engine)
        .map(data => {
          data.period = period;
          data.time = moment(data.date, 'YYYY-MM-DDTHH:mm:ss');
          return data;
        })
    );

    return {
      batchData,
      streamingData
    };
  }

  private _getPeriod(period): Observable<Object> {
    const periodData = this._momentPeriods[period];

    return this._executionService.getExecutionsByDate(
      periodData.requestPeriod,
      moment()
        .subtract(periodData.daysLeft, periodData.substractPeriod)
        .minutes(0)
        .seconds(0)
        .format('YYYY-MM-DDTHH:mm:ss') + 'Z',
      moment()
        .minutes(0)
        .seconds(0)
        .format('YYYY-MM-DDTHH:mm:ss') + 'Z'
    );
  }

  private _getEjecutionsData(times, executionsData): Array<number> {
    const initialEjecutions = Array.from(times, time =>
      Object({
        executions: 0,
        period: time
      })
    );

    executionsData.forEach(execution => {
      const initialExecutionsFiltered: Array<Object> = initialEjecutions.filter(
        batchValue => batchValue.period.isSame(execution.time, this._granularitys[execution.period].singular)
      );

      const initialExecution: any =
        initialExecutionsFiltered && initialExecutionsFiltered.length
          ? initialExecutionsFiltered[0]
          : { executions: 0 };
      initialExecution.executions = execution.total;
    });

    return initialEjecutions.map(data => data.executions);
  }
}
