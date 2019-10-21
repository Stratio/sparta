/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as moment from 'moment';
import {Injectable} from '@angular/core';
import {Info, Parameter, ShowedActions, Status} from '@app/executions/execution-detail/types/execution-detail';
import * as utils from '@utils';
import {ExecutionHelperService} from '../../../../services/helpers/execution.service';
import { DateFormats } from '../../../../models/enums.ts';

@Injectable()
export class ExecutionDetailHelperService {

  constructor(
    private _executionHelperService: ExecutionHelperService
  ) { }

  private _getDuration = function(startDate, endDate){
    const diff = endDate - startDate;
    const duration = moment.duration(diff);
    const days = duration.days();
    const hours = duration.hours();
    const minutes = duration.minutes();
    const seconds = duration.seconds();
    const daysString = days ? days + 'd' : '';
    const hoursString = hours ? hours + 'h' : '';
    const minutesString = minutes ? minutes + 'm' : '';
    const secondsString = seconds ? seconds + 's' : '';

    return `${daysString} ${hoursString} ${minutesString} ${secondsString}`;
  };

  private _getContextMenu(exec): Array<any>{
    const archiveBtn = {
        icon: 'icon-inbox2',
        label: 'Archive',
        id: 'workflow-archive'
    };
    const unArchiveBtn = {
      icon: 'icon-outbox',
      label: 'Unarchive',
      id: 'workflow-unarchive'
    };
    const deleteBtn = {
      icon: 'icon-trash',
      label: 'Delete',
      id: 'workflow-delete'
    };
    const options = [deleteBtn];
    if (exec.status !== 'Running' && !exec.archived) { // && !== Archived
      options.unshift(archiveBtn);
    } else if (exec.status !== 'Running' && exec.archived){
      options.unshift(unArchiveBtn);
    }
    return [{options: options}];
  }

  public getExecutionDetail(execution): Info {
    const filteredStatus = utils.getFilterStatus(execution.resumedStatus);
    const execNorm = this._executionHelperService.normalizeExecution(execution);

    return {
      name: execNorm.name,
      marathonId: execNorm.id,
      description: execNorm.genericDataExecution.workflow.description,
      context: execNorm.context,
      status: filteredStatus,
      executionEngine: execNorm.executionEngine,
      executionType: execNorm. executionType,
      sparkURI: execNorm.sparkURI,
      lastError: execNorm.genericDataExecution.lastError,
      historyServerURI: execNorm.historyServerURI,
      launchHour: execNorm.launchDate ? moment(execNorm.launchDate, DateFormats.executionTimeStampMoment).format(DateFormats.executionHourFormat) : "Not launched",
      launchDate: execNorm.launchDate ? moment(execNorm.launchDate, DateFormats.executionTimeStampMoment).format(DateFormats.executionDateFormat) : "",
      startHour: execNorm.startDate ? moment(execNorm.startDate, DateFormats.executionTimeStampMoment).format(DateFormats.executionHourFormat) : "Not started",
      startDate: execNorm.startDate ? moment(execNorm.startDate, DateFormats.executionTimeStampMoment).format(DateFormats.executionDateFormat) : "",
      duration: (execNorm.startDate && execNorm.endDate) ? this._getDuration(execNorm.startDateMillis, execNorm.endDateMillis) : "Not finished",
      endHour: execNorm.endDate ? moment(execNorm.endDate, DateFormats.executionTimeStampMoment).format(DateFormats.executionHourFormat) : "Not finished",
      endDate: execNorm.endDate ? moment(execNorm.endDate, DateFormats.executionTimeStampMoment).format(DateFormats.executionDateFormat) : "",
    };
  }

  public getExecutionParameters(response): Array<Parameter> {
    const parametersOrigin = response.genericDataExecution &&
      response.genericDataExecution.workflow &&
      response.genericDataExecution.workflow.parametersUsedInExecution ||
      undefined;

    return parametersOrigin ? Object.keys(parametersOrigin).map(parameterKey => {
      const [parameterName, parameterType = 'User defined'] = parameterKey.split('.').reverse();
      const parameterValue = parametersOrigin[parameterKey];

      return {
        name: parameterName,
        lastModified: parameterValue,
        type: parameterType,
        completeName: parameterKey,
        selected: false
      };
    }) : [];
  }

  public getExecutionStatuses(response): Array<Status> {
    const statuses = response.statuses || undefined;
    return statuses.map(status => {

      return {
        name: status.state,
        statusInfo: status.statusInfo,
        startTime: moment(status.lastUpdateDate, DateFormats.executionTimeStampMoment).format(DateFormats.executionTimeStampFormat)
      };
    });
  }

  public getShowedActions(execution): ShowedActions {
    const filteredStatus = utils.getFilterStatus(execution.resumedStatus);
    return {
      showedReRun: !execution.archived && ['Stopped', 'Failed'].includes(filteredStatus),
      showedStop: ['Running', 'Starting'].includes(filteredStatus),
      showedContextMenu: ['Stopped', 'Failed'].includes(filteredStatus),
      menuOptions: this._getContextMenu(execution)
    };
  }

}
