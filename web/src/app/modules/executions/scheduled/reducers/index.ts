/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { ActionReducerMap, combineReducers, createFeatureSelector, State } from '@ngrx/store';
import { InjectionToken } from '@angular/core';

import { orderBy, reduceReducers } from '@utils';

import * as fromRoot from 'reducers';
import * as fromExecutionsList from './executions';

import * as fromScheduled from './scheduled';
import * as fromScheduledFilters from './scheduled-filters';

import * as fromExecution from './execution';

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
export const getSchedulesTimeIntervalFilterValue = createSelector(getSchedulesFiltersState, state => state.workflowTypesFilterValue);

export const getScheduledExecutions = createSelector(getScheduledState, state => state.scheduledExecutionsList);
export const getSelectedExecutions = createSelector(getScheduledState, state => state.selectedExecutions);