/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';

import * as fromRoot from 'reducers';
import * as fromPlugins from './plugins';

export interface PluginsState {
   plugins: fromPlugins.State;
}

export interface State extends fromRoot.State {
   plugins: PluginsState;
}

export const reducers = {
   plugins: fromPlugins.reducer
};

export const getPluginsState = createFeatureSelector<PluginsState>('plugins');

export const getPluginsEntityState = createSelector(
   getPluginsState,
   state => state.plugins
);


// plugins
export const getPluginsList: any = createSelector(getPluginsEntityState, fromPlugins.getPluginsList);
export const getDriversList: any = createSelector(getPluginsEntityState, fromPlugins.getDriversList);
export const getSelectedPlugins: any = createSelector(getPluginsEntityState, fromPlugins.getSelectedPlugins);
export const getSelectedDrivers: any = createSelector(getPluginsEntityState, fromPlugins.getSelectedDrivers);
export const isLoaded: any = createSelector(getPluginsEntityState, fromPlugins.isLoaded);
