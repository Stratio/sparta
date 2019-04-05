/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';
import { Order, StDropDownMenuItem } from '@stratio/egeo';

export enum ScheduledFiltersActions {
  SEARCH_SCHEDULED_EXECUTIONS = '[Executions-scheduled-filters] Search scheduled executions',
  CHANGE_TYPE_FILTER = '[Executions-scheduled-filters] Change type filter',
  CHANGE_ACTIVE_FILTER = '[Executions-scheduled-filters] Change active filter',
  CHANGE_ORDER = '[Executions-scheduled-filters] Change order'

}

export class SearchScheduledExecutions implements Action {
  readonly type = ScheduledFiltersActions.SEARCH_SCHEDULED_EXECUTIONS;
  constructor(public searchQuery: string) { }
}

export class ChangeTypeFilter implements Action {
  readonly type = ScheduledFiltersActions.CHANGE_TYPE_FILTER;
  constructor(public value: StDropDownMenuItem) { }
}

export class ChangeActiveFilter implements Action {
  readonly type = ScheduledFiltersActions.CHANGE_ACTIVE_FILTER;
  constructor(public value: StDropDownMenuItem) { }
}

export class ChangeScheduledOrder implements Action {
  readonly type = ScheduledFiltersActions.CHANGE_ORDER;
  constructor(public order: Order) {}
}

export type ScheduledFiltersUnionActions = SearchScheduledExecutions 
  | ChangeTypeFilter
  | ChangeActiveFilter
  | ChangeScheduledOrder;
