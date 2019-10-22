/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {Action} from '@ngrx/store';
import {WizardNode} from '@app/wizard/models/node';

export const GET_WORKFLOW_DETAIL = '[Workflow detail] Get workflow detail';
export const GET_WORKFLOW_DETAIL_COMPLETE = '[Workflow detail] Get workflow detail complete';
export const GET_WORKFLOW_DETAIL_ERROR = '[Workflow detail] Get workflow detail error';
export const GET_SELECTED_EDGE = '[Workflow detail] Create a list of filtered edges';
export const HIDE_CONFIG_MODAL = '[Workflow detail] Hide config modal';
export const SHOW_CONFIG_MODAL = '[Workflow detail] Show config modal';
export const SET_SELECTED_NODE = '[Workflow detail] Set selected node';

export class GetWorkflowDetailAction implements Action {
  readonly type = GET_WORKFLOW_DETAIL;
  constructor(public versionId: string) {}
}

export class GetWorkflowDetailCompleteAction implements Action {
  readonly type = GET_WORKFLOW_DETAIL_COMPLETE;
  constructor(public workflow: any) {
  }
}

export class GetWorkflowDetailErrorAction implements Action {
  readonly type = GET_WORKFLOW_DETAIL_ERROR;
  constructor() {}
}

export class SelectEdgeAction implements Action {
  readonly type = GET_SELECTED_EDGE;
  constructor(public payload: any) {}
}

export class HideConfigModal implements Action {
  readonly type = HIDE_CONFIG_MODAL;
}

export class ShowConfigModal implements Action {
  readonly type = SHOW_CONFIG_MODAL;
  constructor(public node: WizardNode) { }
}

export class SetSelectedNode implements Action {
  readonly type = SET_SELECTED_NODE;
  constructor(public node: WizardNode) { }
}

export type Actions =
  | GetWorkflowDetailAction
  | GetWorkflowDetailCompleteAction
  | SelectEdgeAction
  | HideConfigModal
  | ShowConfigModal
  | SetSelectedNode;
