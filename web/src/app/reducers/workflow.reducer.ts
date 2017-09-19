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
  workflowNameValidation: {
    validatedName: boolean;
    validatedWorkflow: any
  };
  reload: boolean;
  displayOptions: Array<any>;
  modalOpen: boolean;
  selectedDisplayOption: string;
}

const initialState: State = {
  workflowList: [],
  workflowFilteredList: [],
  selectedWorkflows: [],
  searchQuery: '',
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
    case workflowActions.actionTypes.LIST_WORKFLOW: {
      return Object.assign({}, state, {});
    }
    case workflowActions.actionTypes.LIST_WORKFLOW_COMPLETE: {
      return Object.assign({}, state, {
        workflowList: action.payload,
        reload: true
      });
    }
    case workflowActions.actionTypes.REMOVE_WORKFLOW_SELECTION: {
      return Object.assign({}, state, {
        selectedWorkflows: []
      });
    }
    case workflowActions.actionTypes.SELECT_WORKFLOW: {
      return Object.assign({}, state, {
        selectedWorkflows: [...state.selectedWorkflows, action.payload]
      });
    }
    case workflowActions.actionTypes.DESELECT_WORKFLOW: {
      return Object.assign({}, state, {
        selectedWorkflows: state.selectedWorkflows.filter((workflow: any) => {
              if ( workflow.id !== action.payload.id ) {
                  return workflow;
              }
          })
      });
    }
    case workflowActions.actionTypes.UPDATE_WORKFLOWS_COMPLETE: {
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
    case workflowActions.actionTypes.DELETE_WORKFLOW_COMPLETE: {
      const workflowId = action.payload.id;
      return Object.assign({}, state, {
        workflowList: state.workflowList.filter((workflow: any) => {
          return workflow.id !== workflowId;
        }),
        selectedWorkflows: []
      });
    }
    case workflowActions.actionTypes.FILTER_WORKFLOWS: {
      return Object.assign({}, state, {
        searchQuery: action.payload
      });
    }
    case workflowActions.actionTypes.DISPLAY_MODE: {
      return Object.assign({}, state, {
        selectedDisplayOption: action.payload
      });
    }
    case workflowActions.actionTypes.VALIDATE_WORKFLOW_NAME: {
      return Object.assign({}, state, {
        workflowNameValidation: {
          validatedName: false,
          validatedWorkflow: action.payload
        }
      });
    }
    case workflowActions.actionTypes.VALIDATE_WORKFLOW_NAME_COMPLETE: {
      return Object.assign({}, state, {
        workflowNameValidation: {
          validatedName: true
        }
      });
    }
    case workflowActions.actionTypes.SAVE_JSON_WORKFLOW_COMPLETE: {
      return Object.assign({}, state, {
        modalOpen: true,
        reload: true
      });
    }
    default:
      return state;
  }
}

export const getWorkFlowList: any = (state: State) => state.workflowList;
export const getSelectedWorkflows: any = (state: State) => state.selectedWorkflows;
export const getSearchQuery: any = (state: State) => state.searchQuery;
export const getDisplayOptions: any = (state: State) => state.displayOptions;
export const getSelectedDisplayOption: any = (state: State) => state.selectedDisplayOption;
export const getWorkflowNameValidation: any = (state: State) => state.workflowNameValidation;
export const getWorkflowModalState: any = (state: State) => state.modalOpen;
export const getReloadState: any = (state: State) => state.reload;
