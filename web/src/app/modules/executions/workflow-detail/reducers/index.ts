/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';
import * as workflowDetailActions from '../actions/workflow-detail';
import * as fromRoot from 'reducers';
import { QualityRule, Edge } from '@app/executions/models';

export interface State extends fromRoot.State {
  workflowDetail: WorkflowDetail;
}

export interface WorkflowState {
  workflowDetail: WorkflowDetail;
}

export interface WorkflowDetail {
  execution: any;
  loading: boolean;
  qualityRules: Array<QualityRule>;
  selectedEdge: Edge;
}

const initialState: WorkflowDetail = {
  execution: null,
  loading: false,
  qualityRules: [],
  selectedEdge: {
    origin: '',
    destination: ''
  }
};

export function workflowDetailReducer(state: WorkflowDetail = initialState, action: workflowDetailActions.Actions): WorkflowDetail {
  switch (action.type) {

    case workflowDetailActions.GET_WORKFLOW_DETAIL: {
      return {
        ...state,
        execution: null,
        loading: false
      };
    }

    case workflowDetailActions.GET_WORKFLOW_DETAIL_COMPLETE: {
      return {
        ...state,
        execution: action,
        loading: false
      };
    }

    case workflowDetailActions.GET_QUALITY_RULES_COMPLETE: {
      return {
        ...state,
        qualityRules: action.payload
      };
    }

    case workflowDetailActions.GET_SELECTED_EDGE: {

      return {
        ...state,
        selectedEdge: {
          origin: action.payload.origin.name,
          destination: action.payload.destination.name
        }
      };
    }

    default:
      return state;
  }
}

export const getWorkflowState = createFeatureSelector<WorkflowState>('workflowDetail');

export const workflowDetailReducers = {
  workflowDetail: workflowDetailReducer
};

export const getWorkflowDetail = createSelector(getWorkflowState, state => state.workflowDetail);
export const getWorkflowDetailIsLoading = createSelector(getWorkflowState, state => state.workflowDetail);
export const qualityRulesState = createSelector(getWorkflowDetailIsLoading, state => state.qualityRules);
export const selectedEdgeState = createSelector(getWorkflowDetailIsLoading, state => state.selectedEdge);

export const filteredQualityRulesState = createSelector(selectedEdgeState, qualityRulesState,
  (selectedEdge, qualityRules) => qualityRules.filter(qualityRule =>
    qualityRule.transformationStepName === selectedEdge.origin &&
    qualityRule.outputStepName === selectedEdge.destination) || null
);

export const getExecution = createSelector(
  getWorkflowDetail,
  (workflow: WorkflowDetail) => workflow.execution && workflow.execution.execution || null
);

export const getWorkflowEdges = createSelector(
  getExecution,
  (execution: any) => execution && execution.genericDataExecution ?
    execution.genericDataExecution.workflow.pipelineGraph.edges : []
);

export const getWorkflowNodes = createSelector(
  getExecution,
  (execution: any) => execution && execution.genericDataExecution ?
    execution.genericDataExecution.workflow.pipelineGraph.nodes : []
);

export const getQualityRulesCount = createSelector(
  getWorkflowEdges,
  qualityRulesState,
  (edges: any, qualityRules) => edges.map(edge => qualityRules.filter(qualityRule =>
    qualityRule.transformationStepName === edge.origin &&
    qualityRule.outputStepName === edge.destination).length)
);

export const getQualityRulesStatus = createSelector(
  getWorkflowEdges,
  qualityRulesState,
  (edges: any, qualityRules) => edges.map(edge => qualityRules.filter(qualityRule =>
    qualityRule.transformationStepName === edge.origin &&
    qualityRule.outputStepName === edge.destination).every(qr => qr.status))
);

export const getEdgesMap = createSelector(
  getWorkflowNodes,
  getWorkflowEdges,
  (nodes, edges) => {
    const nodesMap = nodes.reduce(function (map, obj) {
      map[obj.name] = obj;
      return map;
    }, {});
    return edges.map((edge: any) => ({
      origin: nodesMap[edge.origin],
      destination: nodesMap[edge.destination],
      dataType: edge.dataType
    }));
  }
)
