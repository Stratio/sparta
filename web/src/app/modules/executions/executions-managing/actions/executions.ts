/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';

export const LIST_EXECUTIONS = '[Executions Managing] List executions';
export const LIST_EXECUTIONS_COMPLETE = '[Executions Managing] List executions complete';
export const LIST_EXECUTIONS_FAIL = '[Executions Managing] List executions fail';
export const SELECT_EXECUTIONS_ACTION = '[Executions Managing] Select execution';
export const DESELECT_EXECUTIONS_ACTION = '[Executions Managing] Deselect execution';
export const STOP_EXECUTIONS_ACTION = '[Executions Managing] Stop execution';
export const STOP_EXECUTIONS_ACTION_COMPLETE = '[Executions Managing] Stop execution complete';



export class ListExecutionsAction implements Action {
   readonly type = LIST_EXECUTIONS;
}

export class ListExecutionsFailAction implements Action {
   readonly type = LIST_EXECUTIONS_FAIL;
}

export class ListExecutionsCompleteAction implements Action {
   readonly type = LIST_EXECUTIONS_COMPLETE;
   constructor(public payload: any) { }
}

export class SelectExecutionAction implements Action {
   readonly type = SELECT_EXECUTIONS_ACTION;
   constructor(public execution: any) { }
}

export class DeselectExecutionAction implements Action {
   readonly type = DESELECT_EXECUTIONS_ACTION;
   constructor(public execution: any) { }
}

export class StopExecutionAction implements Action {
   readonly type = STOP_EXECUTIONS_ACTION;
}

export class StopExecutionCompleteAction implements Action {
   readonly type = STOP_EXECUTIONS_ACTION_COMPLETE;
}



export type Actions = ListExecutionsAction
   | ListExecutionsFailAction
   | ListExecutionsCompleteAction
   | SelectExecutionAction
   | DeselectExecutionAction;
