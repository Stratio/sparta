///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

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
