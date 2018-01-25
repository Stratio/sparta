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
import * as fromWorkflowList from './workflow-reducer';

export interface LauncherSettingsState {
   workflows: fromWorkflowList.State;
}

export interface State extends fromRoot.State {
   workflows: LauncherSettingsState;
}

export const reducers = {
   workflows: fromWorkflowList.reducer
};

export const getWorkflowsState = createFeatureSelector<LauncherSettingsState>('workflows');
export const getWorkflowsEntityState = createSelector(
   getWorkflowsState,
   state => state.workflows
);

export const getWorkflowList: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getWorkFlowList);
export const getSelectedWorkflows: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getSelectedWorkflows);
export const getWorkflowSearchQuery: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getSearchQuery);
export const getSelectedDisplayOption: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getSelectedDisplayOption);
export const getWorkflowNameValidation: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getWorkflowNameValidation);
export const getReloadState: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getReloadState);
export const getWorkflowModalState: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getWorkflowModalState);
export const getExecutionInfo: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getExecutionInfo);
export const getCurrentGroupLevel: any = createSelector(getWorkflowsEntityState, (state) => state.currentLevel);
export const getGroupsList: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getGroupsList);
export const getCreateGroupModalOpen: any = createSelector(getWorkflowsEntityState, (state) => state.createGroupModalOpen);
export const getJsonValidationErrors: any = createSelector(getWorkflowsEntityState, (state) => state.jsonValidationError);
