/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { ActionReducerMap, combineReducers, createFeatureSelector } from '@ngrx/store';
import { InjectionToken } from '@angular/core';

import { WorkflowUtils } from '../models/utils/utils';
import { orderBy, reduceReducers } from '@utils';

import * as fromRoot from 'reducers';
import * as fromWorkflowList from './workflows';
import * as fromFilters from './filters';
import { crossReducers } from './cross-reducers';

export interface WorkflowsMonitoringState {
   workflows: fromWorkflowList.State;
   filters: fromFilters.State;
}

export interface State extends fromRoot.State {
   workflows: WorkflowsMonitoringState;
}

export const reducers = reduceReducers(combineReducers({
   workflows: fromWorkflowList.reducer,
   filters: fromFilters.reducer
}), crossReducers);

export const reducerToken = new InjectionToken<ActionReducerMap<WorkflowsMonitoringState>>('Reducers');

export function getReducers() {
   return reducers;
}

export const reducerProvider = [
   { provide: reducerToken, useFactory: getReducers }
];
export const getWorkflowsMonitoringState = createFeatureSelector<WorkflowsMonitoringState>('workflows');

export const getWorkflowsState = createSelector(
   getWorkflowsMonitoringState,
   state => state.workflows
);

export const getFiltersState = createSelector(
   getWorkflowsMonitoringState,
   state => state.filters
);

export const getWorkflowsQueryResults = createSelector(
   getWorkflowsState,
   getFiltersState,
   (workflows, filters) => WorkflowUtils.searchWorkflows(workflows.workflowList, filters.searchQuery)
);

export const getWorkflowList = createSelector(
   getWorkflowsQueryResults,
   getFiltersState,
   (workflows, filters) => orderBy([...filters.currentFilterStatus.length ? workflows.filter(workflow =>
      filters.currentFilterStatus === '' || workflow.filterStatus === filters.currentFilterStatus) : workflows],
      filters.workflowOrder.orderBy, filters.workflowOrder.type ? true : false));

export const getMonitoringStatus = createSelector(
   getWorkflowsQueryResults,
   getFiltersState,
   (workflows, filters) => {
      const monitoring = workflows.reduce((map: any, workflow: any) => {
         const status = workflow.filterStatus.toLowerCase();
         map[status] = (map[status] || 0) + 1;
         return map;
      }, Object.create(null));
      monitoring.workflows = workflows.length;
      return monitoring;
   });

export const getSelectedWorkflows = createSelector(getWorkflowsState, fromWorkflowList.getSelectedWorkflows);
export const getExecutionInfo = createSelector(getWorkflowsState, fromWorkflowList.getExecutionInfo);
export const getTableOrder = createSelector(getFiltersState, (state) => state.workflowOrder);
export const getSelectedFilter = createSelector(getFiltersState, (state) => state.currentFilterStatus);
export const getPaginationNumber = createSelector(getFiltersState, (state) => state.paginationOptions);
export const getWorkflowSearchQuery = createSelector(getFiltersState, fromFilters.getSearchQuery);
