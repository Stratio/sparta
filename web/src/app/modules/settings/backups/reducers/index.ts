/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';

import * as fromRoot from 'reducers';
import * as fromBackups from './backups';

export interface BackupsState {
   backups: fromBackups.State;
}

export interface State extends fromRoot.State {
   backups: BackupsState;
}

export const reducers = {
   backups: fromBackups.reducer
};

export const getBackupsState = createFeatureSelector<BackupsState>('backups');

export const getBackupsEntityState = createSelector(
   getBackupsState,
   state => state.backups
);

// backups
export const getBackupList: any = createSelector(getBackupsEntityState, fromBackups.getBackupList);
export const getSelectedBackups: any =  createSelector(getBackupsEntityState, fromBackups.getSelectedBackups);
export const isLoaded: any =  createSelector(getBackupsEntityState, (state) => state.loaded);
