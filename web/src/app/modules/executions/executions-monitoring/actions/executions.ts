/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';

export const LIST_EXECUTIONS = '[Executions Monitoring] List executions';
export const LIST_EXECUTIONS_COMPLETE = '[Executions Monitoring] List executions complete';
export const LIST_EXECUTIONS_FAIL = '[Executions Monitoring] List executions fail';

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

 export type Actions = ListExecutionsAction
  | ListExecutionsFailAction
  | ListExecutionsCompleteAction;
