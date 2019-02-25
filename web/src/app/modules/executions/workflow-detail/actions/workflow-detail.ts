/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {Action} from "@ngrx/store";

export const GET_WORKFLOW_DETAIL = '[Executions] Get workflow detail';
export const GET_WORKFLOW_DETAIL_COMPLETE = '[Executions] Get workflow detail complete';

export class GetWorkflowDetailAction implements Action {
  readonly type = GET_WORKFLOW_DETAIL;
  constructor(public executionId: string) {}
}

export class GetWorkflowDetailCompleteAction implements Action {
  readonly type = GET_WORKFLOW_DETAIL_COMPLETE;
  constructor(public execution: any) {}
}
