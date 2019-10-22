/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';
import { ActionReducerMap } from '@ngrx/store';

import * as fromAlerts from './alerts';
import * as fromGlobal from './global';

export interface State {
    user: fromGlobal.State;
    alerts: fromAlerts.State;
}

export const reducers: ActionReducerMap<State> = {
    user: fromGlobal.reducer,
    alerts: fromAlerts.reducer,
};

export const getUserState: any = (state: State) => state.user;
export const getAlertsState: any = createFeatureSelector<fromAlerts.State>('alerts');


// alerts
export const getCurrentAlert: any = createSelector(getAlertsState, fromAlerts.getCurrentAlert);
export const showPersistentError: any = createSelector(getAlertsState, fromAlerts.showPersistentError);
export const pendingSavedData: any = createSelector(getAlertsState, fromAlerts.pendingSavedData);

// user
export const getUsername: any = createSelector(getUserState, fromGlobal.getUsername);
export const getSparkUILink: any = createSelector(getUserState, fromGlobal.getSparkUILink);
export const getIsCiCdEnabled: any = createSelector(getUserState, fromGlobal.getIsCiCdEnabled);
export const getSpartaTimeout: any = createSelector(getUserState, fromGlobal.getSpartaTimeout);
