/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
export const getExecutionInfo: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getExecutionInfo);
export const getJsonValidationErrors: any = createSelector(getWorkflowsEntityState, (state) => state.jsonValidationError);
export const getMonitoringStatus: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getMonitoringStatus);
export const getSelectedFilter: any = createSelector(getWorkflowsEntityState, (state) => state.currentFilterStatus);
export const getPaginationNumber: any = createSelector(getWorkflowsEntityState, (state) => state.paginationOptions);
export const getTableOrder: any = createSelector(getWorkflowsEntityState, (state) => {
    return {
        orderBy: state.orderBy,
        type: state.sortOrder ? 1 : 0
    };
});
