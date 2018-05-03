/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';

import * as fromRoot from 'reducers';
import * as fromWizard from './wizard';
import * as fromEntities from './entities';
import { WizardEdge, WizardNode } from '@app/wizard/models/node';

export interface WizardState {
   wizard: fromWizard.State;
   entities: fromEntities.State;
}

export interface State extends fromRoot.State {
   wizard: WizardState;
}

export const reducers = {
   wizard: fromWizard.reducer,
   entities: fromEntities.reducer
};

export const getWizardFeatureState = createFeatureSelector<WizardState>('wizard');

export const getWizardState = createSelector(
   getWizardFeatureState,
   state => state.wizard
);

export const getEntitiesState = createSelector(
   getWizardFeatureState,
   state => state.entities
);

export const getEdges = createSelector(
   getWizardState,
   (state) => state.edges
);

export const getWorkflowNodes = createSelector(
   getWizardState,
   (state) => state.nodes
);

export const getWorkflowEdges = createSelector(
   getEdges,
   getWorkflowNodes,
   (edges: WizardEdge[], wNodes: WizardNode[]) => {
      const nodesMap = wNodes.reduce(function (map, obj) {
         map[obj.name] = obj;
         return map;
      }, {});
      return edges.map((edge: WizardEdge) => ({
         origin: nodesMap[edge.origin],
         destination: nodesMap[edge.destination]
      }));
   }
);

export const getErrorsManagementOutputs = createSelector(
   getWorkflowNodes,
   (wNodes) => wNodes.reduce((filtered: Array<string>, workflowNode) => {
      if (workflowNode.stepType === 'Output' && workflowNode.configuration.errorSink) {
         filtered.push(workflowNode.name);
      }
      return filtered;
   }, [])
);

// wizard
export const isCreationMode = createSelector(getEntitiesState, fromEntities.isCreationMode);
export const getMenuOptions = createSelector(getEntitiesState, fromEntities.getMenuOptions);
export const getWorkflowType = createSelector(getEntitiesState, fromEntities.getWorkflowType);
export const getTemplates = createSelector(getEntitiesState, fromEntities.getTemplates);
export const isShowedEntityDetails = createSelector(getWizardState, state => state.showEntityDetails);
export const getSelectedEntities = createSelector(getWizardState, state => state.selectedEntity);
export const getSelectedEntityData = createSelector(getWizardState, fromWizard.getSelectedEntityData);
export const getEditionConfigMode = createSelector(getWizardState, fromWizard.getEditionConfigMode);
export const showSettings = createSelector(getWizardState, state => state.showSettings);
export const isEntitySaved = createSelector(getWizardState, state => state.editionSaved);
export const getWorkflowSettings = createSelector(getWizardState, state => state.settings);
export const getWorkflowPosition = createSelector(getWizardState, state => state.svgPosition);
export const isSavedWorkflow = createSelector(getWizardState, state => state.savedWorkflow);
export const getSelectedRelation = createSelector(getWizardState, state => state.selectedEdge);
export const areUndoRedoEnabled = createSelector(getWizardState, fromWizard.areUndoRedoEnabled);
export const getValidationErrors = createSelector(getWizardState, state => state.validationErrors);
export const isPristine = createSelector(getWizardState, state => state.pristineWorkflow);
export const isLoading = createSelector(getWizardState, state => state.loading);
export const getWorkflowHeaderData = createSelector(getWizardState, fromWizard.getWorkflowHeaderData);
export const getValidatedEntityName = createSelector(getWizardState, state => state.entityNameValidation);
export const isShowedCrossdataCatalog = createSelector(getWizardState, state => state.isShowedCrossdataCatalog);
