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

export interface WorkflowsState {
   workflowsManaging: fromWorkflowList.State;
}

export interface State extends fromRoot.State {
   workflowsManaging: WorkflowsState;
}

export const reducers = {
   workflowsManaging: fromWorkflowList.reducer
};

export const getWorkflowsState = createFeatureSelector<WorkflowsState>('workflowsManaging');

export const getWorkflowsEntityState = createSelector(
   getWorkflowsState,
   state => state.workflowsManaging
);

export const getOpenedWorkflow: any = createSelector(getWorkflowsEntityState, (state) => state.openedWorkflow.name);
export const getWorkflowList: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getWorkFlowList);
export const getWorkflowStatuses: any =  createSelector(getWorkflowsEntityState, (state) => state.workflowsStatus);
export const getWorkflowVersions: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getWorkflowVersions);
export const getSelectedEntity: any =  createSelector(getWorkflowsEntityState, fromWorkflowList.getSelectedEntity);
export const getSelectedGroups: any = createSelector(getWorkflowsEntityState, (state) => state.selectedGroups);
export const getSelectedVersions: any = createSelector(getWorkflowsEntityState, (state) => state.selectedVersions);
export const getSelectedVersionsData: any = createSelector(getWorkflowsEntityState, (state) => state.selectedVersionsData);
export const getSelectedVersion: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getSelectedVersion);
export const getSelectedWorkflows: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getSelectedWorkflows);
export const getWorkflowSearchQuery: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getSearchQuery);
export const getDisplayOptions: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getDisplayOptions);
export const getSelectedDisplayOption: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getSelectedDisplayOption);
export const getWorkflowNameValidation: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getWorkflowNameValidation);
export const getReloadState: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getReloadState);
export const getExecutionInfo: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getExecutionInfo);
export const getShowModal: any = createSelector(getWorkflowsEntityState, (state) => state.showModal);
export const getModalError: any = createSelector(getWorkflowsEntityState, (state) => state.modalError);
export const getCurrentGroupLevel: any = createSelector(getWorkflowsEntityState, (state) => {
    return {
        group: state.currentLevel,
        workflow: state.openedWorkflow ? state.openedWorkflow.name : ''
    };
});
export const getGroupsList: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getGroupsList);
export const getAllGroups: any = createSelector(getWorkflowsEntityState, (state) => state.groups);
export const getLoadingState: any = createSelector(getWorkflowsEntityState, (state) => state.loading);
