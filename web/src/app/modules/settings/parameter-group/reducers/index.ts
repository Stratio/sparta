import { Environment } from '../../../../models/environment';
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';

import * as fromRoot from 'reducers';

import * as fromGlobal from './global';
import * as fromEnvironment from './environment';
import * as fromCustom from './custom';

export interface ParameterGroupState {
   global: fromGlobal.State;
   environment: fromEnvironment.State;
   custom: fromCustom.State;
}

export interface State extends fromRoot.State {
   parameterGroup: ParameterGroupState;
}

export const reducers = {
   global: fromGlobal.reducer,
   environment: fromEnvironment.reducer,
   custom: fromCustom.reducer
};

export const getParameterGroupState = createFeatureSelector<ParameterGroupState>('parameterGroup');


/** Reducers state selectors */

export const getGlobalEntityState = createSelector(
   getParameterGroupState,
   state => state.global
);

export const getEnvironmentEntityState = createSelector(
   getParameterGroupState,
   state => state.environment
);

export const getCustomEntityState = createSelector(
   getParameterGroupState,
   state => state.custom
);

/** */


// Global parameters selectors

export const getGlobalVariables = createSelector(
   getGlobalEntityState,
   state => state.globalVariables
);

// Enviroment parameters selectors

export const getEnvironmentVariables = createSelector(
   getEnvironmentEntityState,
   state => state.environmentVariables
);

export const getEnvironmentContexts = createSelector(
   getEnvironmentEntityState,
   state => state.contexts
);

export const getListId = createSelector(
   getEnvironmentEntityState,
   state => state.list
);

export const getConfigContexts = createSelector(
   getEnvironmentEntityState,
   state => state.configContexts
);

// Custom parameters selectors

export const getCustomVariables = createSelector(
   getCustomEntityState,
   state => state.customVariables
);

export const getCustomList = createSelector(
   getCustomEntityState,
   state => state.customList
);
export const getCustomParams = createSelector(
   getCustomEntityState,
   state => state.customVariables
);

export const getCustomContexts = createSelector(
   getCustomEntityState,
   state => state.contexts
);

export const getSelectedList = createSelector(
   getCustomEntityState,
   state => state.list
);
