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
import * as fromDebug from './debug';
import * as fromExternalData from './externalData';

import { WizardEdge, WizardNode } from '@app/wizard/models/node';

export interface WizardState {
  wizard: fromWizard.State;
  entities: fromEntities.State;
  debug: fromDebug.State;
  externalData: fromExternalData.State;
}

export interface State extends fromRoot.State {
  wizard: WizardState;
}

export const reducers = {
  wizard: fromWizard.reducer,
  entities: fromEntities.reducer,
  debug: fromDebug.reducer,
  externalData: fromExternalData.reducer
};

export const getWizardFeatureState = createFeatureSelector<WizardState>('wizard');

export const getWizardState = createSelector(
  getWizardFeatureState,
  state => state.wizard
);

export const getDebugState = createSelector(
  getWizardFeatureState,
  state => state.debug
);

export const getEntitiesState = createSelector(
  getWizardFeatureState,
  state => state.entities
);

export const getExternalDataState = createSelector(
  getWizardFeatureState,
  state => state.externalData
);

export const getEdges = createSelector(
  getWizardState,
  (state) => state.edges
);

export const getWorkflowNodes = createSelector(
  getWizardState,
  (state) => state.nodes
);

export const getEdgeOptions = createSelector(
  getWizardState,
  (state) => state.edgeOptions
);

export const getWizardNofications = createSelector(
  getEntitiesState,
  (state) => state.notification
);

export const getDebugResult = createSelector(
  getDebugState,
  (state) => state.lastDebugResult
);

export const getServerStepValidation = createSelector(
  getWizardState,
  (state) => state.serverStepValidations
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
      destination: nodesMap[edge.destination],
      dataType: edge.dataType
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

export const getSelectedNodeData = createSelector(getWizardState, fromWizard.getSelectedEntityData);

export const getSelectedNodeSchemas = createSelector(
  getSelectedNodeData,
  getDebugResult,
  getEdges,
  (selectedNode: WizardNode, debugResult: any, edges: any) => {
    if (edges && edges.length && debugResult && debugResult.steps && selectedNode) {
        return {
        inputs: edges.filter(edge => edge.destination === selectedNode.name)
          .map(edge => debugResult.steps[edge.origin]).filter(input => input).sort(),
        output: debugResult.steps[selectedNode.name]
    };
  } else {
    return null;
  }
});

export const getSelectedEntityData = createSelector(
  getSelectedNodeData,
  getDebugResult,
  getServerStepValidation,
  getSelectedNodeSchemas,
  (selectedNode: WizardNode, debugResult: any, serverStepValidation: Array<any>, schemas: any) => {
    const entityData = selectedNode && debugResult && debugResult.steps && debugResult.steps[selectedNode.name] ? {
      ...selectedNode,
      debugResult: debugResult.steps[selectedNode.name]
    } : selectedNode;

    return {
      ...entityData,
      schemas: schemas,
      serverValidationError: selectedNode ? serverStepValidation[selectedNode.name] : {}
    };
  }
);
export const getEditionConfig = createSelector(getWizardState, fromWizard.getEditionConfigMode);

export const getEditionConfigMode = createSelector(
  getEditionConfig,
  getDebugResult,
  getServerStepValidation,
  getSelectedNodeSchemas,
  (editionConfig: any, debugResult: any, stepValidation, schemas: any) => {
    return editionConfig && editionConfig.isEdition ?
      {...editionConfig,
        serverValidation: stepValidation[editionConfig.editionType.data.name],
        debugResult: debugResult && debugResult.steps && debugResult.steps[editionConfig.editionType.data.name],
        schemas: schemas,
      } : editionConfig; }
  );


// wizard
export const getDebugFile = createSelector(getWizardState, state => state.debugFile);
export const getWorkflowId =  createSelector(getWizardState, state => state.workflowId);
export const isCreationMode = createSelector(getEntitiesState, fromEntities.isCreationMode);
export const getMenuOptions = createSelector(getEntitiesState, fromEntities.getMenuOptions);
export const getWorkflowType = createSelector(getEntitiesState, fromEntities.getWorkflowType);
export const getTemplates = createSelector(getEntitiesState, fromEntities.getTemplates);
export const isShowedEntityDetails = createSelector(getWizardState, state => state.showEntityDetails);
export const getSelectedEntities = createSelector(getWizardState, state => state.selectedEntity);
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
export const isWorkflowDebugging = createSelector(getDebugState, state => state.isDebugging);
export const showDebugConsole = createSelector(getDebugState, state => state.showDebugConsole);
export const getDebugConsoleSelectedTab = createSelector(getDebugState, state => state.debugConsoleSelectedTab);
export const getEnvironmentList = createSelector(getExternalDataState, state => state.environmentVariables);
