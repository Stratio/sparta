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

import { WorkflowListType } from 'app/models/workflow.model';
import * as workflowActions from 'actions/workflow';

export interface State {
  workflowList: Array<WorkflowListType>;
  workflowFilteredList: Array<WorkflowListType>;
  searchQuery: String;
  selectedWorkflows: Array<WorkflowListType>;
  selectedWorkflowsIds: Array<string>;
  workflowNameValidation: {
    validatedName: boolean;
    validatedWorkflow: any
  };
  reload: boolean;
  executionInfo: any;
  displayOptions: Array<any>;
  modalOpen: boolean;
  selectedDisplayOption: string;
}

const initialState: State = {
  workflowList: [],
  workflowFilteredList: [],
  selectedWorkflows: [],
  selectedWorkflowsIds: [],
  searchQuery: '',
  executionInfo: null,
  modalOpen: false,
  workflowNameValidation: {
    validatedName: false,
    validatedWorkflow: undefined
  },
  reload: false,
  displayOptions: [
    {
      value: 'BLOCKS',
      icon: 'icon-grid2'
    },
    {
      value: 'ROWS',
      icon: 'icon-menu2'
    }
  ],
  selectedDisplayOption: 'BLOCKS'
};

export function reducer(state: State = initialState, action: any): State {
  switch (action.type) {
    case workflowActions.LIST_WORKFLOW: {
      return Object.assign({}, state, {});
    }
    case workflowActions.LIST_WORKFLOW_COMPLETE: {
      return Object.assign({}, state, {
        workflowList: action.payload,
        reload: true
      });
    }
    case workflowActions.REMOVE_WORKFLOW_SELECTION: {
      return Object.assign({}, state, {
        selectedWorkflows: [],
        selectedWorkflowsIds: []
      });
    }
    case workflowActions.SELECT_WORKFLOW: {
      return Object.assign({}, state, {
        selectedWorkflows: [...state.selectedWorkflows, action.payload],
        selectedWorkflowsIds: [...state.selectedWorkflowsIds, action.payload.id]
      });
    }
    case workflowActions.DESELECT_WORKFLOW: {
      const newSelection = state.selectedWorkflows.filter((workflow: any) => {
        if (workflow.id !== action.payload.id) {
          return workflow;
        }
      });
      return Object.assign({}, state, {
        selectedWorkflows: newSelection,
        selectedWorkflowsIds: newSelection.map((workflow) => {
          return workflow.id;
        })
      });
    }
    case workflowActions.UPDATE_WORKFLOWS_COMPLETE: {
      const context = action.payload;
      return Object.assign({}, state, {
        workflowList: state.workflowList.map((workflow: any) => {
          const c = context.filter((item: any) => {
            return workflow.id === item.id;
          });
          workflow.context = (c && Array.isArray(c) && c.length) ? c[0] : {};
          return workflow;
        })
      });
    }
    case workflowActions.DELETE_WORKFLOW_COMPLETE: {
      return Object.assign({}, state, {
        selectedWorkflows: [],
        selectedWorkflowsIds: []
      });
    }
    case workflowActions.FILTER_WORKFLOWS: {
      return Object.assign({}, state, {
        searchQuery: action.payload
      });
    }
    case workflowActions.DISPLAY_MODE: {
      return Object.assign({}, state, {
        selectedDisplayOption: action.payload
      });
    }
    case workflowActions.VALIDATE_WORKFLOW_NAME: {
      return Object.assign({}, state, {
        workflowNameValidation: {
          validatedName: false,
          validatedWorkflow: action.payload
        }
      });
    }
    case workflowActions.VALIDATE_WORKFLOW_NAME_COMPLETE: {
      return Object.assign({}, state, {
        workflowNameValidation: {
          validatedName: true
        }
      });
    }
    case workflowActions.SAVE_JSON_WORKFLOW_COMPLETE: {
      return Object.assign({}, state, {
        modalOpen: true,
        reload: true
      });
    }
    case workflowActions.GET_WORKFLOW_EXECUTION_INFO_COMPLETE: {
      return Object.assign({}, state, {
        executionInfo: action.payload
      });
    }
    case workflowActions.CLOSE_WORKFLOW_EXECUTION_INFO: {
      return Object.assign({}, state, {
        executionInfo: null
      });
    }
    default:
      return state;
  }
}

export const getWorkFlowList: any = (state: State) => state.workflowList;
export const getSelectedWorkflows: any = (state: State) => {
  return {
    selected: state.selectedWorkflows,
    selectedIds: state.selectedWorkflowsIds
  };
};
export const getSearchQuery: any = (state: State) => state.searchQuery;
export const getDisplayOptions: any = (state: State) => state.displayOptions;
export const getSelectedDisplayOption: any = (state: State) => state.selectedDisplayOption;
export const getWorkflowNameValidation: any = (state: State) => state.workflowNameValidation;
export const getWorkflowModalState: any = (state: State) => state.modalOpen;
export const getReloadState: any = (state: State) => state.reload;
export const getExecutionInfo: any = (state: State) => state.executionInfo;
