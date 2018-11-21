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

export const getNodesMap = createSelector(
  getWorkflowNodes,
  (nodes) => nodes.reduce(function (map, obj) {
    map[obj.name] = obj;
    return map;
  }, {})
);

export const getWorkflowEdges = createSelector(
  getEdges,
  getNodesMap,
  (edges: WizardEdge[], nodesMap: any) => {
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
          .map(edge => edge.dataType === 'ValidData' ? debugResult.steps[edge.origin] : debugResult.steps[edge.origin + '_Discard']).filter(input => input).sort(),
        output: debugResult.steps[selectedNode.name],
        outputs: Object.keys(debugResult.steps)
          .map(key => debugResult.steps[key])
          .filter(output => output.error || (output.result.step && (output.result.step === selectedNode.name || output.result.step === selectedNode.name + '_Discard')))
          .sort((a, b) => a.result && b.result  && a.result.step && a.result.step > b.result.step ? 1 : -1)
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
    if (selectedNode) {
      const entityData = debugResult && debugResult.steps && debugResult.steps[selectedNode.name] ? {
        ...selectedNode,
        debugResult: debugResult.steps[selectedNode.name]
      } : selectedNode;

      return {
        ...entityData,
        schemas: schemas,
        serverValidationError: (serverStepValidation[selectedNode.name]) ? serverStepValidation[selectedNode.name].errors : null
      };
    } else {
      return {};
    }
  }
);
export const getEditionConfig = createSelector(getWizardState, fromWizard.getEditionConfigMode);

export const getEditionConfigMode = createSelector(
  getEditionConfig,
  getDebugResult,
  getServerStepValidation,
  getSelectedNodeSchemas,
  getEdges,(editionConfig: any, debugResult: any, stepValidation, schemas: any, edges: WizardEdge[]) => {
    return editionConfig && editionConfig.isEdition ?
      {
        ...editionConfig,
        serverValidation: (stepValidation[editionConfig.editionType.data.name]) ? stepValidation[editionConfig.editionType.data.name].errors : null,
        serverValidationInternalErrors: (stepValidation[editionConfig.editionType.data.name]) ? stepValidation[editionConfig.editionType.data.name].internalErrors : null,
        inputSteps: edges.filter(edge => edge.destination === editionConfig.editionType.data.name)
          .map(edge => edge.origin),
        debugResult: debugResult && debugResult.steps && debugResult.steps[editionConfig.editionType.data.name],
        schemas: schemas,
      } : editionConfig;
  }
);


export const showDebugConsole = createSelector(getDebugState, state => state.showDebugConsole);

export const getConsoleDebugEntity = createSelector(getDebugState,state => state.showedDebugDataEntity);


export const getConsoleDebugEntityData = createSelector(
  showDebugConsole,
  getConsoleDebugEntity,
  getSelectedNodeData,
  getDebugResult,
  (showConsole, debugEntity, selectedNode, debugResult) => {
    if (!showConsole || !debugResult.steps) {
      return null;
    } else {
      if (selectedNode && debugEntity && debugEntity.length) {
        return {
          ...debugResult.steps[debugEntity],
          debugEntityName: debugEntity
        };
      } else if (selectedNode) {
        return {
          ...debugResult.steps[selectedNode.name],
          debugEntityName: selectedNode.name
        };
      } else {
        return null;
      }
    }
  });

// wizard
export const getDebugFile = createSelector(getWizardState, state => state.debugFile);
export const getWorkflowId = createSelector(getWizardState, state => state.workflowId);
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
export const getDebugConsoleSelectedTab = createSelector(getDebugState, state => state.debugConsoleSelectedTab);
export const getEnvironmentList = createSelector(getExternalDataState, state => state.environmentVariables);
export const getCustomGroups = createSelector(getExternalDataState, state => state.customGroups);
export const isShowingDebugConfig = createSelector(getDebugState, state => state.showExecutionConfig);
export const getExecutionContexts = createSelector(getDebugState, state => state.executionContexts);

export const getParameters = createSelector(getExternalDataState, state => ({
  globalVariables: state.globalVariables,
  environmentVariables: state.environmentVariables,
  customGroups: state.customGroups
}));
