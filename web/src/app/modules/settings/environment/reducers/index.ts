/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';

import * as fromRoot from 'reducers';
import * as fromEnvironment from './environment';

export interface EnvironmentState {
   environment: fromEnvironment.State;
}

export interface State extends fromRoot.State {
   environment: EnvironmentState;
}

export const reducers = {
   environment: fromEnvironment.reducer
};

export const getEnvironmentState = createFeatureSelector<EnvironmentState>('environment');

export const getEnvironmentEntityState = createSelector(
   getEnvironmentState,
   state => state.environment
);

// environment
export const getEnvironmentList: any = createSelector(getEnvironmentEntityState, fromEnvironment.getEnvironmentList);
