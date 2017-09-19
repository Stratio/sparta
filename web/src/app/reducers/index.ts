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
import { ActionReducer, combineReducers } from '@ngrx/store';
import { compose } from '@ngrx/core/compose';
import { WorkflowsType as WorkflowsTypeState } from '../models/workflow.model';
import { InputType as InputTypeState } from '../models/input.model';
import { OutputType as OutputTypeState } from '../models/output.model';
import { BackupType as BackupTypeState } from '../models/backup.model';

import * as fromWorkflow from './workflow.reducer';
import * as fromInput from './input';
import * as fromOutput from './output';
import * as fromBackups from './backups';
import * as fromResources from './resources';
import * as fromCrossdata from './crossdata';
import * as fromAlerts from './alerts';
import * as fromWizard from './wizard';

export interface State {
    workflows: any;
    inputList: any;
    outputList: any;
    backups: any;
    resources: any;
    crossdata: any;
    alerts: any;
    wizard: any;
}

const reducers: any = {
    workflows: fromWorkflow.reducer,
    inputList: fromInput.reducer,
    outputList: fromOutput.reducer,
    backups: fromBackups.reducer,
    resources: fromResources.reducer,
    crossdata: fromCrossdata.reducer,
    alerts: fromAlerts.reducer,
    wizard: fromWizard.reducer
};

const developmentReducer: ActionReducer<State> = compose(combineReducers)(reducers);
const productionReducer: ActionReducer<State> = combineReducers(reducers);

export function reducer(state: any, action: any): any {
    return productionReducer(state, action);
}

export const getWorkflowsState: any = (state: State) => state.workflows;
export const getInputListState: any = (state: State) => state.inputList;
export const getOutputListState: any = (state: State) => state.outputList;
export const getBackupsState: any = (state: State) => state.backups;
export const getResourcesState: any = (state: State) => state.resources;
export const getCrossdataState: any = (state: State) => state.crossdata;
export const getAlertsState: any = (state: State) => state.alerts;
export const getWizardState: any = (state: State) => state.wizard;


// workflows
export const getWorkflowList: any = createSelector(getWorkflowsState, fromWorkflow.getWorkFlowList);
export const getSelectedWorkflows: any = createSelector(getWorkflowsState, fromWorkflow.getSelectedWorkflows);
export const getWorkflowSearchQuery: any = createSelector(getWorkflowsState, fromWorkflow.getSearchQuery);
export const getDisplayOptions: any = createSelector(getWorkflowsState, fromWorkflow.getDisplayOptions);
export const getSelectedDisplayOption: any = createSelector(getWorkflowsState, fromWorkflow.getSelectedDisplayOption);
export const getWorkflowNameValidation: any = createSelector(getWorkflowsState, fromWorkflow.getWorkflowNameValidation);
export const getReloadState: any = createSelector(getWorkflowsState, fromWorkflow.getReloadState);
export const getWorkflowModalState: any = createSelector(getWorkflowsState, fromWorkflow.getWorkflowModalState);

// inputs
export const getInputList: any = createSelector(getInputListState, fromInput.getInputList);
export const getSelectedInputs: any = createSelector(getInputListState, fromInput.getSelectedInputs);
export const getSelectedInputDisplayOption: any = createSelector(getInputListState, fromInput.getSelectedDisplayOption);
export const getEditedInput: any = createSelector(getInputListState, fromInput.getEditedInput);


// outputs
export const getOutputList: any = createSelector(getOutputListState, fromOutput.getOutputList);
export const getSelectedOutputs: any = createSelector(getOutputListState, fromOutput.getSelectedOutputs);
export const getSelectedOutputDisplayOption: any = createSelector(getOutputListState, fromOutput.getSelectedDisplayOption);
export const getEditedOutput: any = createSelector(getOutputListState, fromOutput.getEditedOutput);


// backups
export const getBackupList: any = createSelector(getBackupsState, fromBackups.getBackupList);

// resources
export const getPluginsList: any = createSelector(getResourcesState, fromResources.getPluginsList);
export const getDriversList: any = createSelector(getResourcesState, fromResources.getDriversList);


// crossdata
export const getTablesList: any = createSelector(getCrossdataState, fromCrossdata.getTableList);

// alerts
export const getCurrentAlert: any = createSelector(getAlertsState, fromAlerts.getCurrentAlert);


// wizard
export const isCreationMode: any = createSelector(getWizardState, fromWizard.isCreationMode);
export const isShowedEntityDetails: any = createSelector(getWizardState, fromWizard.isShowedEntityDetails);
export const getMenuOptions: any = createSelector(getWizardState, fromWizard.getMenuOptions);
export const getSelectedEntities: any = createSelector(getWizardState, fromWizard.getSelectedEntities);
export const getWorkflowRelations: any = createSelector(getWizardState, fromWizard.getWorkflowRelations);
export const getWorkflowNodes: any = createSelector(getWizardState, fromWizard.getWorkflowNodes);
