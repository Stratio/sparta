/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { ActionReducerMap, combineReducers, createFeatureSelector } from '@ngrx/store';
import { InjectionToken } from '@angular/core';

import { orderBy, reduceReducers } from '@utils';

import * as fromRoot from 'reducers';
import * as fromExecutionsList from './executions';

export interface ExecutionsMonitoringState {
  executions: fromExecutionsList.State;
}

export interface State extends fromRoot.State {
  executions: ExecutionsMonitoringState;
}

export const reducers = reduceReducers(combineReducers({ executions: fromExecutionsList.reducer }));

export const reducerToken = new InjectionToken<ActionReducerMap<ExecutionsMonitoringState>>('Reducers');

export function getReducers() {
  return reducers;
}

export const reducerProvider = [
  { provide: reducerToken, useFactory: getReducers }
];
export const getExecutionsMonitoringState = createFeatureSelector<ExecutionsMonitoringState>('executions');

export const getExecutionsState = createSelector(
  getExecutionsMonitoringState,
  state => state.executions
);

export const getStatusFilter = createSelector(getExecutionsState, state => state.statusFilter);
export const getTypeFilter = createSelector(getExecutionsState, state => state.typeFilter);
export const getTimeIntervalFilter = createSelector(getExecutionsState, state => state.timeIntervalFilter);
export const getSearchQuery = createSelector(getExecutionsState, state => state.searchQuery);
export const getExecutionsList = createSelector(getExecutionsState, state => state.executionList);
export const getCurrentPage = createSelector(getExecutionsState, state => state.pagination.pageNumber);
export const getPerPageElements = createSelector(getExecutionsState, state => state.pagination.perPage);
export const getTableOrder = createSelector(getExecutionsState, state => state.order);

export const getFilteredExecutionsList = createSelector(
  getExecutionsList,
  getStatusFilter,
  getTypeFilter,
  getTimeIntervalFilter,
  (executions, statusFilter, typeFilter, timeIntervalFilter) => {
    const filters = [];
    if (statusFilter.length) {
      filters.push((execution) => execution.status === statusFilter);
    }
    if (typeFilter.length) {
      filters.push((execution) => execution.executionEngine === typeFilter);
    }
    if (timeIntervalFilter > 0) {
      const current = new Date().getTime();
      filters.push((execution) => execution.startDateMillis > (current - timeIntervalFilter));
    }
    return filters.length ? executions.filter(execution => !(filters.map(filter => filter(execution)).indexOf(false) > -1)) : executions;
  }
);

export const getFilteredSearchExecutionsList = createSelector(
  getFilteredExecutionsList,
  getSearchQuery,
  getTableOrder,
  (executions, searchQuery, order) => {
    const query = searchQuery.toLowerCase();
    return orderBy(searchQuery.length ? executions.filter(execution => {
      return execution.name.toLowerCase().indexOf(query) > -1 || execution.status.toLowerCase().indexOf(query) > -1;
    }) : executions, order.orderBy, order.type ? true : false);
  });

export const getSelectedExecutions = createSelector(
  getExecutionsState,
  state => state.selectedExecutionsIds
);


