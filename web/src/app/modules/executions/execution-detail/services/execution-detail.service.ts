/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as moment from 'moment';
import {Injectable} from "@angular/core";
import {Info, Parameter, ShowedActions, Status} from "@app/executions/execution-detail/types/execution-detail";
import * as utils from "@utils";
import {ExecutionHelperService} from "../../../../services/helpers/execution.service";

@Injectable()
export class ExecutionDetailHelperService {

  constructor(
    private _executionHelperService: ExecutionHelperService
  ) { }

  private _getDuration = function(startDate, endDate){
    let diff = endDate - startDate;
    let duration = moment.duration(diff);
    return duration.hours() + ":" + duration.minutes() + ":" + duration.seconds() + "." + duration.milliseconds();
  }

  private _getContextMenu(exec): Array<any>{
    let archiveBtn = {
        icon: 'icon-inbox2',
        label: 'Archive',
        id: 'workflow-archive'
    };
    let unArchiveBtn = {
      icon: 'icon-outbox',
      label: 'Unarchive',
      id: 'workflow-unarchive'
    };
    let deleteBtn = {
      icon: 'icon-trash',
      label: 'Delete',
      id: 'workflow-delete'
    }
    let options = [deleteBtn];
    if (exec.status !== "Running" && !exec.archived){ // && !== Archived
      options.unshift(archiveBtn);
    } else if (exec.status !== "Running" && exec.archived){
      options.unshift(unArchiveBtn);
    }
    return [{options: options}];
  }

  public getExecutionDetail(execution): Info {
    let filteredStatus = utils.getFilterStatus(execution.resumedStatus);
    let execNorm = this._executionHelperService.normalizeExecution(execution);

    return {
      name: execNorm.name,
      marathonId: execNorm.id,
      description: execNorm.genericDataExecution.workflow.description,
      context: execNorm.context,
      status: filteredStatus,
      executionEngine: execNorm.executionEngine,
      sparkURI: execNorm.sparkURI,
      historyServerURI: execNorm.historyServerURI,
      launchHour: execNorm.launchDate ? moment(new Date(execNorm.launchDate)).format('HH:mm:ss') : "Not launched",
      launchDate: execNorm.launchDate ? moment(new Date(execNorm.launchDate)).format('DD/MM/YYYY') : "",
      startHour: execNorm.startDate ? moment(new Date(execNorm.startDate)).format('HH:mm:ss') : "Not started",
      startDate: execNorm.startDate ? moment(new Date(execNorm.startDate)).format('DD/MM/YYYY') : "",
      duration: (execNorm.startDate && execNorm.endDate) ? this._getDuration(execNorm.startDateMillis, execNorm.endDateMillis) : "Not finished",
      endHour: execNorm.endDate ? moment(new Date(execNorm.endDate)).format('HH:mm:ss') : "Not finished",
      endDate: execNorm.endDate ? moment(new Date(execNorm.endDate)).format('DD/MM/YYYY') : "",
    };
  }

  public getExecutionParameters(response): Array<Parameter> {
    let parametersOrigin = response.genericDataExecution &&
      response.genericDataExecution.workflow &&
      response.genericDataExecution.workflow.parametersUsedInExecution ||
      undefined;
    return Object.keys(parametersOrigin).map(parameterKey => {
      const [parameterName, parameterType = 'User defined'] = parameterKey.split('.').reverse();
      const parameterValue = parametersOrigin[parameterKey];

      return {
        name: parameterName,
        lastModified: parameterValue,
        type: parameterType,
        completeName: parameterKey,
        selected: false
      };
    });
  }

  public getExecutionStatuses(response): Array<Status> {
    let statuses = response.statuses || undefined;
    return statuses.map(status => {
      return {
        name: status.state,
        statusInfo: status.statusInfo,
        startTime: moment(status.lastUpdateDate).format('DD/MM/YYYY hh:mm:ss')
      };
    });
  }

  public getShowedActions(execution): ShowedActions {
    let filteredStatus = utils.getFilterStatus(execution.resumedStatus);
    return {
      showedReRun: !execution.archived && ['Stopped', 'Failed'].includes(filteredStatus),
      showedStop: ['Running', 'Starting'].includes(filteredStatus),
      showedContextMenu: ['Stopped', 'Failed'].includes(filteredStatus),
      menuOptions: this._getContextMenu(execution)
    };
  }

}
