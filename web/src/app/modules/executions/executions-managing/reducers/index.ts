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

import * as fromExecution from './execution';

export interface ExecutionsMonitoringState {
  executions: fromExecutionsList.State;
}

export interface State extends fromRoot.State {
  executions: ExecutionsMonitoringState;
}

export const reducers = reduceReducers(combineReducers({ 
  executions: fromExecutionsList.reducer, 
  execution: fromExecution.reducer
}));

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
export const getCurrentPage = createSelector(getExecutionsState, state => state.pagination.currentPage);
export const getPerPageElements = createSelector(getExecutionsState, state => state.pagination.perPage);
export const getTotalElements = createSelector(getExecutionsState, state => state.pagination.total);
export const getTableOrder = createSelector(getExecutionsState, state => state.order);
export const getExecutionInfo = createSelector(getExecutionsState, state => state.executionInfo);
export const getArchivedExecutions = createSelector(getExecutionsState, state => state.archivedExecutionList);
export const isArchivedPage = createSelector(getExecutionsState, state => state.isArchivedPage);
export const getIsLoading = createSelector(getExecutionsState, state => state.loading );
export const isEmptyList = createSelector(getExecutionsState, state => state.isArchivedPage ?
  !state.archivedExecutionList.length && !state.loadingArchived : !state.executionList.length && !state.loading);
export const isEmptyFilter = createSelector(getExecutionsState, state => state.statusFilter === '' && state.typeFilter === '' && state.searchQuery === '' && state.timeIntervalFilter === 0 );


export const getFilteredExecutionsList = createSelector(
  getExecutionsList,
  isArchivedPage,
  getArchivedExecutions,
  (executions, archivedPage, archivedExecutions) => {
    const filters = [];
    executions = archivedPage ? archivedExecutions : executions;
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
      return execution.name.toLowerCase().indexOf(query) > -1 || execution.filterStatus.toLowerCase().indexOf(query) > -1;
    }) : executions, order.orderBy, order.type ? true : false);
  });


export const getSelectedExecutions = createSelector(
  getExecutionsState,
  state => state.selectedExecutionsIds
);

export const getLastSelectedExecution = createSelector(
  getSelectedExecutions,
  getFilteredSearchExecutionsList,
  (selectedIds, executions) => {
    const lastId = selectedIds[0];
    return lastId ? executions.find(execution => execution.id === lastId) : null;
  }
);

export const showStopButton = createSelector(
  getExecutionsList,
  getSelectedExecutions,
  getStatusFilter,
  (executions, selectedExecutions, statusFilter) => {
    if (selectedExecutions.length && (!statusFilter.length || statusFilter !== 'Archived')) {
      if (executions.filter(execution => selectedExecutions.indexOf(execution.id) > -1 && execution.filterStatus !== 'Running').length) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  });

export const showArchiveButton = createSelector(
  getExecutionsList,
  getSelectedExecutions,
  isArchivedPage,
  getStatusFilter,
  (executions,  selectedExecutions, archivedPage, statusFilter) => {
    if (selectedExecutions.length && !archivedPage) {
      if (executions.filter(execution => selectedExecutions.indexOf(execution.id) > -1 && execution.filterStatus === 'Running').length) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  });

export const showUnarchiveButton = createSelector(
  getExecutionsList,
  getSelectedExecutions,
  getStatusFilter,
  isArchivedPage,
  (executions, selectedExecutions, statusFilter, archivedPage) => {
    if (selectedExecutions.length && archivedPage) {
      if (executions.filter(execution => selectedExecutions.indexOf(execution.id) > -1 && execution.filterStatus === 'Running').length) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  });

export const getAllSelectedStates = createSelector(
  getFilteredExecutionsList,
  getSelectedExecutions,
  (executions, selectedExecutions) => executions.length === selectedExecutions.length
);
