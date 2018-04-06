/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as workflowActions from './../actions/workflows';
import * as filtersActions from '../actions/filters';

import { MonitoringWorkflow } from './../models/workflow';
import { ExecutionInfo } from './../models/execution-info';

export interface State {
  workflowList: Array<MonitoringWorkflow>;
  selectedWorkflows: Array<MonitoringWorkflow>;
  selectedWorkflowsIds: Array<string>;
  executionInfo: ExecutionInfo;
}

const initialState: State = {
  workflowList: [],
  selectedWorkflows: [],
  selectedWorkflowsIds: [],
  executionInfo: null,
};

export function reducer(state: State = initialState, action: any): State {
  switch (action.type) {
    case workflowActions.LIST_WORKFLOW_COMPLETE: {
      return {
        ...state,
        workflowList: action.payload,
        selectedWorkflows: state.selectedWorkflowsIds.length ?
        action.payload.filter(workflow => state.selectedWorkflowsIds.indexOf(workflow.id) > -1) : [],
        selectedWorkflowsIds: [...state.selectedWorkflowsIds]
      };
    }
    case workflowActions.REMOVE_WORKFLOW_SELECTION: {
      return {
        ...state,
        selectedWorkflows: [],
        selectedWorkflowsIds: []
      };
    }
    case workflowActions.SELECT_WORKFLOW: {
      return {
        ...state,
        selectedWorkflows: [action.payload, ...state.selectedWorkflows],
        selectedWorkflowsIds: [action.payload.id, ...state.selectedWorkflowsIds]
      };
    }
    case workflowActions.DESELECT_WORKFLOW: {
      const newSelection = state.selectedWorkflows.filter(workflow => workflow.id !== action.payload.id);
      return {
        ...state,
        selectedWorkflows: newSelection,
        selectedWorkflowsIds: newSelection.map(workflow => workflow.id)
      };
    }
    case workflowActions.DELETE_WORKFLOW_COMPLETE: {
      return {
        ...state,
        selectedWorkflows: [],
        selectedWorkflowsIds: []
      };
    }
    case filtersActions.CHANGE_ORDER: {
      return {
        ...state,
        selectedWorkflows: [],
        selectedWorkflowsIds: []
      };
    }
    case workflowActions.GET_WORKFLOW_EXECUTION_INFO_COMPLETE: {
      return {
        ...state,
        executionInfo: action.payload
      };
    }
    case workflowActions.CLOSE_WORKFLOW_EXECUTION_INFO: {
      return {
        ...state,
        executionInfo: null
      };
    }
    case workflowActions.RESET_SELECTION: {
      return {
        ...state,
        selectedWorkflows: [],
        selectedWorkflowsIds: [],
      };
    }
    default:
      return state;
  }
}

export const getSelectedWorkflows = (state: State) => {
  return {
    selected: state.selectedWorkflows,
    selectedIds: state.selectedWorkflowsIds
  };
};
export const getExecutionInfo = (state: State) => state.executionInfo;
