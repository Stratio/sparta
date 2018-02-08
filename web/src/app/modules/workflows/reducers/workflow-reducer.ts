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
import * as workflowActions from '../actions/workflow-list';
import { orderBy } from 'utils';

export interface State {
  currentLevel: string;
  groups: Array<any>[];
  createGroupModalOpen: boolean;
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
  jsonValidationError: boolean;
  executionInfo: any;
  modalOpen: boolean;
  selectedDisplayOption: string;
  sortOrder: boolean;
  orderBy: string;
}

const initialState: State = {
  currentLevel: 'default',
  groups: [],
  createGroupModalOpen: false,
  workflowList: [],
  workflowFilteredList: [],
  selectedWorkflows: [],
  selectedWorkflowsIds: [],
  searchQuery: '',
  executionInfo: null,
  modalOpen: false,
  jsonValidationError: false,
  sortOrder: true,
  orderBy: 'name',
  workflowNameValidation: {
    validatedName: false,
    validatedWorkflow: undefined
  },
  reload: false,
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
    case workflowActions.LIST_GROUPS_COMPLETE: {
      return Object.assign({}, state, {
        groups: action.payload
      });
    }
    case workflowActions.CHANGE_GROUP_LEVEL: {
      return Object.assign({}, state, {
        currentLevel: action.payload
      });
    }
    case workflowActions.INIT_CREATE_GROUP: {
      return Object.assign({}, state, {
        createGroupModalOpen: true
      });
    }
    case workflowActions.CREATE_GROUP_COMPLETE: {
      return Object.assign({}, state, {
        createGroupModalOpen: false
      });
    }
    case workflowActions.REMOVE_WORKFLOW_SELECTION: {
      return Object.assign({}, state, {
        selectedWorkflows: [],
        selectedWorkflowsIds: [],
        sortOrder: true,
        orderBy: 'name'
      });
    }
    case workflowActions.SELECT_WORKFLOW: {
      return Object.assign({}, state, {
        selectedWorkflows: [action.payload, ...state.selectedWorkflows],
        selectedWorkflowsIds: [action.payload.id, ...state.selectedWorkflowsIds]
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
        }),
        selectedWorkflows: Object.assign([], state.selectedWorkflows)
      });
    }
    case workflowActions.SAVE_JSON_WORKFLOW_ERROR: {
      return Object.assign({}, state, {
        jsonValidationError: true
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
    case workflowActions.RESET_JSON_MODAL: {
      return Object.assign({}, state, {
        modalOpen: false,
        jsonValidationError: false
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
    case workflowActions.CHANGE_ORDER: {
      return Object.assign({}, state, {
        orderBy: action.payload.orderBy,
        sortOrder: action.payload.sortOrder
      });
    }
    default:
      return state;
  }
}

export const getWorkFlowList: any = (state: State) => orderBy(Object.assign([], state.workflowList), state.orderBy, state.sortOrder);
export const getSelectedWorkflows: any = (state: State) => {
  return {
    selected: state.selectedWorkflows,
    selectedIds: state.selectedWorkflowsIds
  };
};
export const getSearchQuery: any = (state: State) => state.searchQuery;
export const getSelectedDisplayOption: any = (state: State) => state.selectedDisplayOption;
export const getWorkflowNameValidation: any = (state: State) => state.workflowNameValidation;
export const getWorkflowModalState: any = (state: State) => state.modalOpen;
export const getReloadState: any = (state: State) => state.reload;
export const getExecutionInfo: any = (state: State) => state.executionInfo;
export const getGroupsList: any = (state: State) => state.groups.filter((group: any) => {
  if (group.name === state.currentLevel) {
    return false;
  }
  if (state.currentLevel === 'default') {
    return group.name.indexOf('#') === -1;
  } else {
      const split = group.name.split(state.currentLevel + '#');
      return split.length === 2 && split[0] === '' && split[1].indexOf('#') === -1;
  }
}).map((group: any) => {
  const split = group.name.split('#');
  return Object.assign({}, group, {
    label: split.length > 1 ? split[split.length - 1] : group.name
  });
});
