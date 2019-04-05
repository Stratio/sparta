/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';
import { Order } from '@stratio/egeo';
import { ScheduledExecution } from '../models/scheduled-executions';

export enum ScheduledActions {
  LIST_SCHEDULED_EXECUTIONS = '[Executions-scheduled] List scheduled executions',
  LIST_SCHEDULED_EXECUTIONS_COMPLETE = '[Executions-scheduled] List scheduled executions complete',
  LIST_SCHEDULED_EXECUTIONS_FAILED = '[Executions-scheduled] List scheduled executions failed',
  CANCEL_LIST_SCHEDULED_EXECUTIONS = '[Executions-scheduled] Cancel List scheduled executions',

  TOGGLE_EXECUTION_SELECTION = '[Executions-scheduled] Toggle execution selection',

  DELETE_SCHEDULED_EXECUTION = '[Executions-scheduled] Delete scheduled execution',
  DELETE_SCHEDULED_EXECUTION_COMPLETE = '[Executions-scheduled] Delete scheduled execution complete',
  DELETE_SCHEDULED_EXECUTION_FAILED = '[Executions-scheduled] Delete scheduled execution failed',

  START_SCHEDULED_EXECUTION = '[Executions-scheduled] Start scheduled execution',
  START_SCHEDULED_EXECUTION_COMPLETE = '[Executions-scheduled] Start scheduled execution complete',
  START_SCHEDULED_EXECUTION_FAILED = '[Executions-scheduled] Start scheduled execution failed',

  STOP_SCHEDULED_EXECUTION = '[Executions-scheduled] Stop scheduled execution',
  STOP_SCHEDULED_EXECUTION_COMPLETE = '[Executions-scheduled] Stop scheduled execution complete',
  STOP_SCHEDULED_EXECUTION_FAILED = '[Executions-scheduled] Stop scheduled execution failed',

  REMOVE_SELECTION = '[Executions-scheduled] Remove selection'

}

export class ListScheduledExecutionsAction implements Action {
  readonly type = ScheduledActions.LIST_SCHEDULED_EXECUTIONS;
}

export class ListScheduledExecutionsCompleteAction implements Action {
  readonly type = ScheduledActions.LIST_SCHEDULED_EXECUTIONS_COMPLETE;
  constructor(public scheduledExecutions: Array<any>) {}
}

export class ListScheduledExecutionsFailedAction implements Action {
  readonly type = ScheduledActions.LIST_SCHEDULED_EXECUTIONS_FAILED;
}

export class CancelListScheduledExecutionsAction implements Action {
  readonly type = ScheduledActions.CANCEL_LIST_SCHEDULED_EXECUTIONS;
}

export class ToggleExecutionSelectionAction implements Action {
  readonly type = ScheduledActions.TOGGLE_EXECUTION_SELECTION;
  constructor(public executionId: string) { }
}

export class DeleteScheduledExecution implements Action {
  readonly type = ScheduledActions.DELETE_SCHEDULED_EXECUTION;
  constructor(public executionId: string) { }
}

export class DeleteScheduledExecutionComplete implements Action {
  readonly type = ScheduledActions.DELETE_SCHEDULED_EXECUTION_COMPLETE;
}

export class DeleteScheduledExecutionFailed implements Action {
  readonly type = ScheduledActions.DELETE_SCHEDULED_EXECUTION_FAILED;
  constructor(public error: any) { }
}

export class StartScheduledExecution implements Action {
  readonly type = ScheduledActions.START_SCHEDULED_EXECUTION;
  constructor(public execution: ScheduledExecution) { }
}

export class StartScheduledExecutionComplete implements Action {
  readonly type = ScheduledActions.START_SCHEDULED_EXECUTION_COMPLETE;
}

export class StartScheduledExecutionFailed implements Action {
  readonly type = ScheduledActions.START_SCHEDULED_EXECUTION_FAILED;
  constructor(public error: any) { }
}

export class StopScheduledExecution implements Action {
  readonly type = ScheduledActions.STOP_SCHEDULED_EXECUTION;
  constructor(public execution: ScheduledExecution) { }
}

export class StopScheduledExecutionComplete implements Action {
  readonly type = ScheduledActions.STOP_SCHEDULED_EXECUTION_COMPLETE;
}

export class StopScheduledExecutionFailed implements Action {
  readonly type = ScheduledActions.STOP_SCHEDULED_EXECUTION_FAILED;
  constructor(public error: any) { }
}

export class RemoveSelection implements Action {
  readonly type = ScheduledActions.REMOVE_SELECTION;
}


export type ScheduledUnionActions = ListScheduledExecutionsAction
  | ListScheduledExecutionsCompleteAction
  | ListScheduledExecutionsFailedAction
  | CancelListScheduledExecutionsAction
  | ToggleExecutionSelectionAction
  | DeleteScheduledExecution
  | DeleteScheduledExecutionComplete
  | DeleteScheduledExecutionFailed
  | StartScheduledExecution
  | StartScheduledExecutionComplete
  | StartScheduledExecutionFailed
  | StopScheduledExecution
  | StopScheduledExecutionComplete
  | StopScheduledExecutionFailed
  | RemoveSelection;
