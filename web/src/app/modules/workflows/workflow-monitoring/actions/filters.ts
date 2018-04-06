/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';
import { Order } from '@stratio/egeo';

export const CHANGE_ORDER = '[Workflow] Change order';
export const CHANGE_FILTER = '[Workflow] Change filter';
export const SEARCH_WORKFLOWS = '[Workflow] Search workflow';
export const SET_PAGINATION_NUMBER = '[Workflow] Set pagination number';

export class ChangeOrderAction implements Action {
  readonly type = CHANGE_ORDER;
  constructor(public payload: Order) { }
}

export class ChangeFilterAction implements Action {
  readonly type = CHANGE_FILTER;
  constructor(public payload: any) {}
}

export class SearchWorkflowsAction implements Action {
  readonly type = SEARCH_WORKFLOWS;
  constructor(public payload: any) {}
}

export class SetPaginationNumber implements Action {
  readonly type = SET_PAGINATION_NUMBER;
  constructor(public payload: any) {}
}

export type Actions = ChangeOrderAction
  | ChangeFilterAction
  | SearchWorkflowsAction
  | SetPaginationNumber;
