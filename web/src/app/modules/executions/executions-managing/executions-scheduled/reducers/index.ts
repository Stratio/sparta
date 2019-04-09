/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { createFeatureSelector, State } from '@ngrx/store';

import { orderBy } from '@utils';

import * as fromRoot from 'reducers';

import * as fromScheduled from './scheduled';
import * as fromScheduledFilters from './scheduled-filters';

import { ScheduledExecution } from '../models/scheduled-executions';

export interface ScheduledState {
  scheduled: fromScheduled.State;
  scheduledFilters: fromScheduledFilters.State;
}

export interface State extends fromRoot.State {
  scheduled: ScheduledState;
}

export const reducers = {
  scheduled: fromScheduled.reducer,
  scheduledFilters: fromScheduledFilters.reducer
};

export const getExecutionsMonitoringState = createFeatureSelector<ScheduledState>('scheduled');

export const getScheduledState = createSelector(
  getExecutionsMonitoringState,
  state => state.scheduled
);

export const getSchedulesFiltersState = createSelector(
  getExecutionsMonitoringState,
  state => state.scheduledFilters
);


export const getSchedulesWorkflowTypesFilterValue = createSelector(getSchedulesFiltersState, state => state.workflowTypesFilterValue);
export const getActiveFilterValue = createSelector(getSchedulesFiltersState, state => state.activeFilterValue);

export const getScheduledExecutions = createSelector(getScheduledState, state => state.scheduledExecutionsList);
export const getSelectedExecutions = createSelector(getScheduledState, state => state.selectedExecutions);
export const isEmptyScheduledExecutions = createSelector(getScheduledExecutions, executions => !(!!executions.length));
export const getSearchQuery = createSelector(getSchedulesFiltersState, state => state.searchQuery);
export const getTableOrder = createSelector(getSchedulesFiltersState, state => state.order);

export const getScheduledSearchedExecutions = createSelector(
  getScheduledExecutions,
  getSearchQuery,
  (executions: ScheduledExecution[], searchQuery: string) => searchQuery.length ?
    executions.filter(execution => {
      const searchQueryLowercase = searchQuery.toLowerCase();
      return execution.name.toLowerCase().includes(searchQueryLowercase);
    }) : executions);

export const getSheduledSearchedFilteredExecutions = createSelector(
  getScheduledSearchedExecutions,
  getSchedulesWorkflowTypesFilterValue,
  getActiveFilterValue,
  (executions, workflowType, active) => active.value.length || workflowType.value.length ?
    executions.filter(execution => {
      if (active.value.length && execution.active !== (active.value === 'active')) {
        return false;
      }
      if (workflowType.value.length && execution.executionEngine !== workflowType.value) {
        return false;
      }
      return true;
    }) : executions)


export const getScheduledFilteredSearchExecutionsList = createSelector(
  getSheduledSearchedFilteredExecutions,
  getTableOrder,
  (executions, order) => {
    return orderBy(executions, order.orderBy, order.type ? true : false);
  });


  export const getSidebarExecutionDetail = createSelector(
    getSelectedExecutions,
    getScheduledExecutions,
    (selectedExecutions, executions) => selectedExecutions.length ? 
      executions.find(execution => execution.id === selectedExecutions[selectedExecutions.length - 1]): null
  )