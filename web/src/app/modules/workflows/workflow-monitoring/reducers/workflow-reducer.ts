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
import { orderBy, formatDate, getFilterStatus } from '@utils';

export interface State {
  workflowList: Array<WorkflowListType>;
  filteredWorkflow: Array<any>;
  currentFilterStatus: string;
  searchQuery: string;
  paginationOptions: any;
  selectedWorkflows: Array<WorkflowListType>;
  selectedWorkflowsIds: Array<string>;
  workflowNameValidation: {
    validatedName: boolean;
    validatedWorkflow: any
  };
  jsonValidationError: boolean;
  executionInfo: any;
  selectedDisplayOption: string;
  sortOrder: boolean;
  orderBy: string;
}

const initialState: State = {
  workflowList: [],
  filteredWorkflow: [],
  currentFilterStatus: '',
  selectedWorkflows: [],
  selectedWorkflowsIds: [],
  searchQuery: '',
  paginationOptions: {
    currentPage: 1,
    perPage: 10
  },
  executionInfo: null,
  jsonValidationError: false,
  sortOrder: true,
  orderBy: 'name',
  workflowNameValidation: {
    validatedName: false,
    validatedWorkflow: undefined
  },
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
        selectedWorkflows: [...state.selectedWorkflows],
        reload: true,
        filteredWorkflow: getFilteredWorkflow(action.payload, state.searchQuery)
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
    case workflowActions.SEARCH_WORKFLOWS: {
      return Object.assign({}, state, {
        searchQuery: action.payload,
        filteredWorkflow: getFilteredWorkflow(state.workflowList, action.payload),
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
        sortOrder: action.payload.sortOrder,
        selectedWorkflows: [],
        selectedWorkflowsIds: []
      });
    }
    case workflowActions.CHANGE_FILTER: {
      return Object.assign({}, state, {
        currentFilterStatus: action.payload,
        paginationOptions: {...state.paginationOptions, currentPage: 1 }
      });
    }
    case workflowActions.RESET_SELECTION: {
      return Object.assign({}, state, {
        selectedWorkflows: [],
        selectedWorkflowsIds: [],
      });
    }
    case workflowActions.SET_PAGINATION_NUMBER: {
      return Object.assign({}, state, {
        paginationOptions: action.payload
      });
    }
    default:
      return state;
  }
}

export const getWorkFlowList: any = (state: State) => orderBy(Object.assign([], (
  state.currentFilterStatus.length ? state.filteredWorkflow.filter((workflow: any) => {
    const status = workflow.filterStatus;
    return (state.currentFilterStatus === '' || status === state.currentFilterStatus);
  }) : state.filteredWorkflow)), state.orderBy, state.sortOrder);


export const getSelectedWorkflows: any = (state: State) => {
  return {
    selected: state.selectedWorkflows,
    selectedIds: state.selectedWorkflowsIds
  };
};
export const getSearchQuery: any = (state: State) => state.searchQuery;
export const getSelectedDisplayOption: any = (state: State) => state.selectedDisplayOption;
export const getWorkflowNameValidation: any = (state: State) => state.workflowNameValidation;
export const getExecutionInfo: any = (state: State) => state.executionInfo;
export const getMonitoringStatus: any = (state: State) => {
  const monitoring = state.filteredWorkflow.reduce((map: any, workflow: any) => {
    const status = workflow.filterStatus.toLowerCase();
    map[status] = (map[status] || 0) + 1;
    return map;
  }, Object.create(null));
  monitoring.workflows = state.filteredWorkflow.length;
  return monitoring;
};

function getFilteredWorkflow(workflowList: Array<any>, searchQuery: string) {
  return searchQuery.length ? workflowList.filter((workflow: any) => {
    let search = false;
    const status = workflow.status.status === 'Started' ? 'Running' : workflow.status.status;
    const query = searchQuery.toLowerCase();
    const queryNoSpaces = query.replace(' ', '');
    if (('v' + workflow.version + ' - ' + workflow.name).toLowerCase().indexOf(query) > -1) {
      search = true;
    } else if (workflow.tagsAux && workflow.tagsAux.toLowerCase().indexOf(query) > -1) {
      search = true;
    } else if (workflow.group && workflow.group.indexOf(query) > -1) {
      search = true;
    } else if (workflow.executionEngine.toLowerCase().indexOf(query) > -1) {
      search = true;
    } else if (status.toLowerCase().indexOf(queryNoSpaces) > -1) {
      search = true;
    } else if (workflow.filterStatus && workflow.filterStatus.toLowerCase().indexOf(queryNoSpaces) > -1) {
      search = true;
    }

    return search;
  }) : workflowList;
}

