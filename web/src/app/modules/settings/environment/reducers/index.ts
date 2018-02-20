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
