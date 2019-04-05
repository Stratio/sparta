/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';
import { Order } from '@stratio/egeo';

export enum ScheduledActions {
  LIST_SCHEDULED_EXECUTIONS = '[Executions-scheduled] List scheduled executions',
  LIST_SCHEDULED_EXECUTIONS_COMPLETE = '[Executions-scheduled] List scheduled executions complete',
  LIST_SCHEDULED_EXECUTIONS_FAILED = '[Executions-scheduled] List scheduled executions failed',
  CANCEL_LIST_SCHEDULED_EXECUTIONS = '[Executions-scheduled] Cancel List scheduled executions',

  TOGGLE_EXECUTION_SELECTION = '[Executions-scheduled] Toggle execution selection'
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

export type ScheduledUnionActions = ListScheduledExecutionsAction
  | ListScheduledExecutionsCompleteAction
  | ListScheduledExecutionsFailedAction
  | CancelListScheduledExecutionsAction
  | ToggleExecutionSelectionAction;
