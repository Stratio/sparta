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
import { ActionReducer, combineReducers, createFeatureSelector } from '@ngrx/store';
import { ActionReducerMap } from '@ngrx/store';

import * as fromResources from './resources';
import * as fromAlerts from './alerts';
import * as fromGlobal from './global';
import * as fromWizard from './wizard';

export interface State {
    user: fromGlobal.State;
    resources: fromResources.State;
    alerts: fromAlerts.State;
    wizard: fromWizard.State;
}

export const reducers: ActionReducerMap<State> = {
    user: fromGlobal.reducer,
    resources: fromResources.reducer,
    alerts: fromAlerts.reducer,
    wizard: fromWizard.reducer,
};

// const developmentReducer: ActionReducer<State> = compose(combineReducers)(reducers);
const productionReducer: ActionReducer<State> = combineReducers(reducers);

export function reducer(state: any, action: any): any {
    return productionReducer(state, action);
}

export const getUserState: any = (state: State) => state.user;
export const getResourcesState: any = createFeatureSelector<fromResources.State>('resources');
export const getAlertsState: any = createFeatureSelector<fromAlerts.State>('alerts');
export const getWizardState: any = createFeatureSelector<fromWizard.State>('wizard');

// resources
export const getPluginsList: any = createSelector(getResourcesState, fromResources.getPluginsList);
export const getDriversList: any = createSelector(getResourcesState, fromResources.getDriversList);
export const getSelectedPlugins: any = createSelector(getResourcesState, fromResources.getSelectedPlugins);
export const getSelectedDrivers: any = createSelector(getResourcesState, fromResources.getSelectedDrivers);
export const isLoaded: any = createSelector(getResourcesState, fromResources.isLoaded);

// alerts
export const getCurrentAlert: any = createSelector(getAlertsState, fromAlerts.getCurrentAlert);
export const showPersistentError: any = createSelector(getAlertsState, fromAlerts.showPersistentError);
export const pendingSavedData: any = createSelector(getAlertsState, fromAlerts.pendingSavedData);

// wizard
export const isCreationMode: any = createSelector(getWizardState, fromWizard.isCreationMode);
export const isShowedEntityDetails: any = createSelector(getWizardState, fromWizard.isShowedEntityDetails);
export const getMenuOptions: any = createSelector(getWizardState, fromWizard.getMenuOptions);
export const getSelectedEntities: any = createSelector(getWizardState, fromWizard.getSelectedEntities);
export const getSelectedEntityData: any = createSelector(getWizardState, fromWizard.getSelectedEntityData);
export const getWorkflowRelations: any = createSelector(getWizardState, fromWizard.getWorkflowRelations);
export const getWorkflowNodes: any = createSelector(getWizardState, fromWizard.getWorkflowNodes);
export const getEditionConfigMode: any = createSelector(getWizardState, fromWizard.getEditionConfigMode);
export const showSettings: any = createSelector(getWizardState, fromWizard.showSettings);
export const isEntitySaved: any = createSelector(getWizardState, fromWizard.isEntitySaved);
export const getWorkflowSettings: any = createSelector(getWizardState, fromWizard.getWorkflowSettings);
export const getWorkflowName: any = createSelector(getWizardState, fromWizard.getWorkflowName);
export const getWorkflowPosition: any = createSelector(getWizardState, fromWizard.getWorkflowPosition);
export const isSavedWorkflow: any = createSelector(getWizardState, fromWizard.isSavedWorkflow);
export const getSelectedRelation: any = createSelector(getWizardState, fromWizard.getSelectedRelation);
export const areUndoRedoEnabled: any = createSelector(getWizardState, fromWizard.areUndoRedoEnabled);
export const getValidationErrors: any = createSelector(getWizardState, fromWizard.getValidationErrors);
export const isPristine: any = createSelector(getWizardState, fromWizard.isPristine);
export const getWorkflowType: any = createSelector(getWizardState, fromWizard.getWorkflowType);
export const isLoading: any = createSelector(getWizardState, fromWizard.isLoading);
export const getWorkflowHeaderData: any = createSelector(getWizardState, fromWizard.getWorkflowHeaderData);
export const getValidatedEntityName: any = createSelector(getWizardState, fromWizard.getValidatedEntityName);
export const getErrorsManagementOutputs: any = createSelector(getWizardState, fromWizard.getErrorsManagementOutputs);
export const getTemplates: any = createSelector(getWizardState, fromWizard.getTemplates);

// user
export const getUsername: any = createSelector(getUserState, fromGlobal.getUsername);
export const getSparkUILink: any = createSelector(getUserState, fromGlobal.getSparkUILink);
