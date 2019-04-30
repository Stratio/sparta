/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { formatDate } from '@utils';

@Injectable()
export class ExecutionHelperService {

  public normalizeQualityRules(qualityRules) {

    if (qualityRules && qualityRules.length) {
      return qualityRules.map(qualityRule => {
        const {
          id,
          qualityRuleName: name,
          conditionsString: description,
          conditionThreshold: threshold,
          satisfied: status,
          numTotalEvents: totalRows,
          numPassedEvents: rowsPassed,
          numDiscardedEvents: rowsFailed,
          globalAction,
          warning,
          successfulWriting,
          sentToApi,
          conditionsString: condition,
          outputStepName,
          transformationStepName,
          metadataPath
        } = qualityRule;

        return {
          id,
          name,
          description,
          threshold,
          status,
          totalRows,
          rowsPassed,
          rowsFailed,
          successfulWriting,
          qualityScore: ((rowsPassed / totalRows) * 100).toFixed(2).toString() + '%',
          globalAction,
          sentToApi,
          warning,
          condition,
          outputStepName,
          transformationStepName,
          metadataPath,
          satisfiedMessage: status ?
          'OK' :
          'KO',
          satisfiedIcon: status ?
          'icon-dot status-icon success-color' :
          'icon-dot status-icon error-color'
          ,
          warningIcon: warning ?
          'icon-alert warning-color' :
          ''
        };
      });
    }

    return [];
  }

   public normalizeExecution(execution) {
      const {
        id,
        statuses,
        marathonExecution,
        localExecution,
        genericDataExecution: {
          lastError,
          endDate,
          startDate,
          launchDate,
          workflow: {
            name,
            group,
            tags,
            version,
            executionEngine
          },
          executionContext: {
            paramsLists: context = []
          }
        } ,
        totalCount
      } = execution;
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
      let launchDateMillis = 0;
      let launchDateFormatted = '-';
      try {
         launchDateMillis = new Date(launchDate).getTime();
         launchDateFormatted = formatDate(launchDate);
      } catch (error) { }
      const status = statuses.length && statuses[0].state;
      let executionTime = '';
      if (endDateMillis > 0 && endDateMillis > 0) {
         executionTime = this._msToTime(endDateMillis - startDateMillis);
      }
      const filterStatus = this._getFilterStatus(status);
      return {
         id,
         name,
         version,
         sparkURI: localExecution ? localExecution.sparkURI : marathonExecution && marathonExecution.sparkURI || '',
         historyServerURI: localExecution ? localExecution.historyServerURI : marathonExecution && marathonExecution.historyServerURI || '',
         endDate,
         endDateMillis,
         endDateFormatted,
         executedFromScheduler: !!execution.executedFromScheduler,
         launchDate,
         launchDateMillis,
         launchDateFormatted,
         startDate,
         startDateMillis,
         startDateFormatted,
         group,
         executionEngine,
         executionTime,
         lastError,
         genericDataExecution: execution.genericDataExecution,
         tagsAux: tags ? tags.join() : '',
         context: context,
         status,
         startDateWithStatus: filterStatus === 'Running' ?  new Date().getTime() : launchDateMillis,
         statusData: statuses.length && statuses[0],
         filterStatus,
         totalCount
      };
   }

   private _getFilterStatus(status: string) {
      switch (status) {
         case 'Stopping': case 'StoppingByUser': case 'StoppedByUser': case 'Stopped': case 'Finished': case 'NotDefined': case 'Created': case 'NotStarted': case 'Killed':
            return 'Stopped';
         case 'Running': case 'Launched': case 'Starting': case 'Started': case 'Uploaded':
            return 'Running';
         default:
            return status;
      }
   }

   private _msToTime(duration: number) {
      let seconds = duration / 1000;
      const hours = parseInt((seconds / 3600).toString()); // 3,600 seconds in 1 hour
      seconds = seconds % 3600; // seconds remaining after extracting hours
      const minutes = parseInt((seconds / 60).toString()); // 60 seconds in 1 minute
      seconds = seconds % 60;
      let res = '';
      if (hours > 0) {
         res += hours + ' hour';
         res += hours === 1 ? '' : 's';
         res += ', ';
      }
      if (minutes > 0) {
         res += minutes + ' minute';
         res += minutes === 1 ? '' : 's';
         res += ', ';
      }
      res += seconds + ' second';
      res += seconds === 1 ? '' : 's';
      return res;
   }
}
