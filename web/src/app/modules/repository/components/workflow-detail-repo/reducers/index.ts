/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';
import * as workflowDetailActions from '../actions/workflow-detail-repo';
import * as fromRoot from 'reducers';
import { Edge } from '@app/executions/models';
import {WizardEdge, WizardNode} from '@app/wizard/models/node';

export interface State extends fromRoot.State {
  workflowDetail: WorkflowDetail;
}

export interface WorkflowState {
  workflowDetail: WorkflowDetail;
}

export interface WorkflowDetail {
  loading: boolean;
  workflowName: string;
  executionEngine: string;
  ciCdLabel: string;
  uiSettings: any;
  selectedEdge: Edge;
  selectedNode: WizardNode;
  showModal: boolean;
  nodes: Array<WizardNode>;
  edges: Array<WizardEdge>;
}

const initialState: WorkflowDetail = {
  loading: false,
  workflowName: '',
  executionEngine: '',
  ciCdLabel: '',
  uiSettings: null,
  selectedEdge: {
    origin: '',
    destination: ''
  },
  selectedNode: null,
  showModal: false,
  nodes: [],
  edges: []
};


export function workflowDetailRepoReducer(state: WorkflowDetail = initialState, action: workflowDetailActions.Actions): WorkflowDetail {
  switch (action.type) {
    case workflowDetailActions.GET_WORKFLOW_DETAIL: {
      return {
        ...state,
        workflowName: '',
        executionEngine: '',
        uiSettings: null,
        ciCdLabel: '',
        nodes: [],
        edges: [],
        loading: false
      };
    }
    case workflowDetailActions.GET_WORKFLOW_DETAIL_COMPLETE: {
      return {
        ...state,
        workflowName: action.workflow.name,
        executionEngine: action.workflow.executionEngine,
        nodes: action.workflow.pipelineGraph.nodes,
        edges: action.workflow.pipelineGraph.edges,
        ciCdLabel: action.workflow.ciCdLabel,
        uiSettings: action.workflow.uiSettings,
        loading: false
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
    case workflowDetailActions.SHOW_CONFIG_MODAL: {
      console.log('SHOW_CONFIG_MODAL', action);
      return {
        ...state,
        showModal: true,
        selectedNode: action.node
      };
    }
    case workflowDetailActions.HIDE_CONFIG_MODAL: {
      return {
        ...state,
        showModal: false
      };
    }
    case workflowDetailActions.SET_SELECTED_NODE: {
      return {
        ...state,
        selectedNode: action.node,
        selectedEdge: null
      };
    }
    default:
      return state;
  }
}

export const getWorkflowState = createFeatureSelector<WorkflowState>('workflowDetailRepo');

export const workflowDetailRepoReducers = {
  workflowDetail: workflowDetailRepoReducer
};

export const getWorkflowDetail = createSelector(getWorkflowState, state => state.workflowDetail);

export const getWorkflowNodes = createSelector(getWorkflowDetail, workflow => workflow.nodes);
export const getWorkflowEdges = createSelector(getWorkflowDetail, workflow => workflow.edges);
export const getWorkflowExecutionEngine = createSelector(getWorkflowDetail, workflow => workflow.executionEngine);
export const getWorkflowName = createSelector(getWorkflowDetail, workflow => workflow.workflowName);
export const getWorkflowUISettings = createSelector(getWorkflowDetail, workflow => workflow.uiSettings);
export const getWorkflowCICDLabel = createSelector(getWorkflowDetail, workflow => workflow.ciCdLabel);

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
);

export const getShowModal = createSelector(
  getWorkflowDetail,
  state => state.showModal
);
export const getSelectedNode = createSelector(getWorkflowDetail, state => state.selectedNode);
export const getSelectedNodeOutputNames = createSelector(
  getWorkflowNodes,
  getShowModal,
  getSelectedNode,
  (nodes: WizardNode[], showConfigModal: boolean, selectedNode: WizardNode) => {
    if (!showConfigModal) {
      return;
    }

    if (!selectedNode.outputsWriter) {
      return {};
    }
    const names = selectedNode.outputsWriter.map(writer => writer.outputStepName);
    return nodes.reduce((acc, node) => {
      if (names.includes(node.name)) {
        acc[node.name] = {
          name: node.name,
          classPrettyName: node.classPrettyName
        };
      }
      return acc;
    }, {});
  });
