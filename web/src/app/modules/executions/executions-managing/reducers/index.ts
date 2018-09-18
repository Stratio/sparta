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

export const getExecutionsList = createSelector(
   getExecutionsState,
   state => state.executionList
);


export const getSelectedExecutions = createSelector(
   getExecutionsState,
   state => state.selectedExecutionsIds
);


