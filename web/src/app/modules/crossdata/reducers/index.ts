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
import * as fromCrossdata from './crossdata';

export interface CrossdataState {
   crossdata: fromCrossdata.State;
}

export interface State extends fromRoot.State {
   crossdata: CrossdataState;
}

export const reducers = {
   crossdata: fromCrossdata.reducer
};

export const getCrossdataState = createFeatureSelector<CrossdataState>('crossdata');

export const getCrossdataEntityState = createSelector(
   getCrossdataState,
   state => state.crossdata
);


// crossdata
export const getTablesList: any = createSelector(getCrossdataEntityState, fromCrossdata.getTableList);
export const getDatabases: any = createSelector(getCrossdataEntityState, fromCrossdata.getDatabases);
export const getSelectedDatabase: any =  createSelector(getCrossdataEntityState, fromCrossdata.getSelectedDatabase);
export const getQueryResult: any = createSelector(getCrossdataEntityState, fromCrossdata.getQueryResult);
export const getQueryError: any = createSelector(getCrossdataEntityState, fromCrossdata.getQueryError);
export const getSelectedTables: any = createSelector(getCrossdataEntityState, fromCrossdata.getSelectedTables);
export const isLoadingDatabases: any =  createSelector(getCrossdataEntityState, fromCrossdata.isLoadingDatabases);
export const isLoadingTables: any = createSelector(getCrossdataEntityState, fromCrossdata.isLoadingTables);
export const isLoadingQuery: any = createSelector(getCrossdataEntityState, fromCrossdata.isLoadingQuery);