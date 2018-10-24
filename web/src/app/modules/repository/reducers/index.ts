/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';

import * as fromRoot from 'reducers';
import * as fromWorkflowList from './workflows';
import * as fromOrder from './order';
import { orderBy } from '@utils';
import { FOLDER_SEPARATOR } from './../workflow.constants';

export interface WorkflowsState {
    workflowsManaging: fromWorkflowList.State;
    order: fromOrder.State;
}

export interface State extends fromRoot.State {
    workflowsManaging: WorkflowsState;
}

export const reducers = {
    workflowsManaging: fromWorkflowList.reducer,
    order: fromOrder.reducer
};

export const getWorkflowsState = createFeatureSelector<WorkflowsState>('workflowsManaging');

export const getWorkflowsEntityState = createSelector(
    getWorkflowsState,
    state => state.workflowsManaging
);

export const getOrderState = createSelector(
    getWorkflowsState,
    state => state.order
);

export const getNotificationMessage = createSelector(
    getWorkflowsEntityState,
    state => state.notification
);

export const getCurrentLevel = createSelector(getWorkflowsEntityState, state => state.currentLevel);
export const getWorkflowList = createSelector(getWorkflowsEntityState, state => state.workflowList);
export const getWorkflowVersionsList = createSelector(getWorkflowsEntityState, state => state.workflowsVersionsList);
export const getGroupsList = createSelector(getWorkflowsEntityState, state => state.groups);
export const getSearchQuery = createSelector(getWorkflowsEntityState, state => state.searchQuery);

export const getOpenedWorkflow = createSelector(
    getWorkflowsEntityState,
    (state) => state.openedWorkflow ? state.openedWorkflow : undefined);

export const getCurrentGroups = createSelector(
    getGroupsList,
    getCurrentLevel,
    (groups, currentLevel) => groups.filter((group: any) => {
        if (group.name === currentLevel.name) {
            return false;
        } else {
            const split = group.name.split(currentLevel.name + FOLDER_SEPARATOR);
            return split.length === 2 && split[0] === '' && split[1].indexOf(FOLDER_SEPARATOR) === -1;
        }
    }).map((group: any) => {
        const split = group.name.split(FOLDER_SEPARATOR);
        return Object.assign({}, group, {
            label: split.length > 1 ? split[split.length - 1] : group.name
        });
    }));

export const getWorkflowVersions = createSelector(
   getWorkflowVersionsList,
   getOpenedWorkflow,
   (workflowList, openedWorkflow) => {
      const workflowVersions = openedWorkflow ?
         workflowList.filter((workflow: any) =>
            openedWorkflow.name === workflow.name && openedWorkflow.group && openedWorkflow.group.id === workflow.group.id) : [];
      return workflowVersions.length ? workflowVersions[0].versions : workflowVersions;
   });
export const getOrder = createSelector(getOrderState, state => state.sortOrder);
export const getOrderVersions = createSelector(getOrderState, state => state.sortOrderVersions);

export const getWorkflowsOrderedList = createSelector(
    getWorkflowVersionsList,
    getOrder,
    getSearchQuery,
    (workflows, order, searchQuery) => {
        searchQuery = searchQuery.toLowerCase();
        return orderBy([...(searchQuery.length ?
            workflows.filter(workflow => workflow.name.toLowerCase().indexOf(searchQuery) > -1) : workflows)], order.orderBy, order.type ? true : false)
    });

export const getGroupsOrderedList = createSelector(
    getCurrentGroups,
    getOrder,
    getSearchQuery,
    (groups, order, searchQuery) => {
        searchQuery = searchQuery.toLowerCase();
        return orderBy([...(searchQuery.length ?
            groups.filter(group => group.name.toLowerCase().indexOf(searchQuery) > -1) : groups)], order.orderBy, order.type ? true : false)
    });

export const getVersionsOrderedList = createSelector(
    getWorkflowVersions,
    getOrderVersions,
    getSearchQuery,
    (versions, order, searchQuery) => {
        searchQuery = searchQuery.toLowerCase();
        return orderBy([...(searchQuery.length ?
            versions.filter(version => ('v' + version.version).indexOf(searchQuery) > -1) : versions)], order.orderBy, order.type ? true : false)
    });

export const getWorkflowStatuses: any = createSelector(getWorkflowsEntityState, (state) => state.workflowsStatus);
export const getSelectedEntity: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getSelectedEntity);
export const getSelectedGroups: any = createSelector(getWorkflowsEntityState, (state) => state.selectedGroups);
export const getSelectedVersions: any = createSelector(getWorkflowsEntityState, (state) => state.selectedVersions);
export const getSelectedVersionsData: any = createSelector(getWorkflowsEntityState, (state) => state.selectedVersionsData);
export const getSelectedVersion: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getSelectedVersion);
export const getSelectedWorkflows: any = createSelector(getWorkflowsEntityState, fromWorkflowList.getSelectedWorkflows);
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
export const getAllGroups: any = createSelector(getWorkflowsEntityState, (state) => state.groups);
export const getLoadingState: any = createSelector(getWorkflowsEntityState, (state) => state.loading);
export const getSavingState: any = createSelector(getWorkflowsEntityState, (state) => state.saving);
export const getShowExecutionConfig = createSelector(getWorkflowsEntityState, state => state.showExecutionConfig);
export const getExecutionContexts = createSelector(getWorkflowsEntityState, state => state.executionContexts);
