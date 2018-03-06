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
import { FOLDER_SEPARATOR } from './../workflow.constants';
import { orderBy, formatDate } from '@utils';
import { homeGroup } from '@app/shared/constants/global';
import { DataDetails } from './../models/data-details';

export interface State {
  currentLevel: any;
  groups: Array<any>[];
  loading: boolean;
  workflowList: Array<WorkflowListType>;
  workflowsVersionsList: Array<any>;
  workflowsStatus: any;
  searchQuery: String;
  openedWorkflow: any;
  selectedWorkflows: Array<string>;
  selectedGroups: Array<string>;
  selectedVersions: Array<string>;
  selectedVersionsData: Array<any>;
  selectedEntities: Array<any>;
  workflowNameValidation: {
    validatedName: boolean;
    validatedWorkflow: any
  };
  reload: boolean;
  executionInfo: any;
  displayOptions: Array<any>;
  selectedDisplayOption: string;
  sortOrder: boolean;
  orderBy: string;
  showModal: boolean;
  modalError: string;
  sortOrderVersions: boolean;
  orderByVersions: string;
}

const initialState: State = {
  currentLevel: homeGroup,
  groups: [],
  loading: true,
  workflowList: [],
  workflowsVersionsList: [],
  workflowsStatus: {},
  selectedWorkflows: [],
  openedWorkflow: null,
  selectedGroups: [],
  selectedVersions: [],
  selectedVersionsData: [],
  selectedEntities: [],
  searchQuery: '',
  executionInfo: null,
  sortOrder: true,
  orderBy: 'name',
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
  selectedDisplayOption: 'BLOCKS',
  showModal: true,
  modalError: '',
  sortOrderVersions: true,
  orderByVersions: 'version'
};

export function reducer(state: State = initialState, action: any): State {
  switch (action.type) {
    case workflowActions.LIST_GROUP_WORKFLOWS_COMPLETE: {
      const workflows: any = {};
      action.payload.forEach((version: any) => {
        const lastUpdate = version.lastUpdateDate ? version.lastUpdateDate : version.creationDate;
        const lastUpdateDate = new Date(lastUpdate).getTime();
        version.lastUpdateAux = lastUpdateDate;
        version.lastUpdate = formatDate(lastUpdate);
        if (workflows[version.name]) {
          if (workflows[version.name].lastUpdateAux < lastUpdateDate) {
            workflows[version.name].lastUpdateAux = lastUpdateDate;
            workflows[version.name].lastUpdate = formatDate(lastUpdate);
          }
          workflows[version.name].versions.push(version);
        } else {
          workflows[version.name] = {
            name: version.name,
            type: version.executionEngine,
            group: version.group,
            lastUpdateAux: lastUpdateDate,
            lastUpdate: formatDate(lastUpdate),
            versions: [version]
          };
        }
      });
      return Object.assign({}, state, {
        workflowList: action.payload,
        loading: false,
        workflowsVersionsList: Object.keys(workflows).map(function (key) {
          return workflows[key];
        }),
        selectedVersions: [...state.selectedVersions],
        reload: true
      });
    }
    case workflowActions.LIST_GROUP_WORKFLOWS_FAIL: {
      return Object.assign({}, state, {
        loading: false
      });
    }
    case workflowActions.LIST_GROUPS_COMPLETE: {
      return Object.assign({}, state, {
        groups: action.payload
      });
    }
    case workflowActions.CHANGE_GROUP_LEVEL: {
      return Object.assign({}, state, {
        loading: true
      });
    }
    case workflowActions.CHANGE_GROUP_LEVEL_COMPLETE: {
      return Object.assign({}, state, {
        currentLevel: action.payload,
        selectedWorkflows: [],
        openedWorkflow: null,
        workflowList: [],
        workflowsVersionsList: [],
        selectedVersions: [],
        selectedVersionsData: [],
        selectedEntities: [],
        selectedGroups: []
      });
    }
    case workflowActions.REMOVE_WORKFLOW_SELECTION: {
      return Object.assign({}, state, {
        selectedWorkflows: [],
        selectedVersions: [],
        selectedVersionsData: [],
        selectedGroups: [],
        sortOrder: true,
        orderBy: 'name'
      });
    }
    case workflowActions.SELECT_WORKFLOW: {
      const isSelected = state.selectedWorkflows.indexOf(action.payload) > -1;
      return Object.assign({}, state, {
        selectedWorkflows: isSelected ?
          state.selectedWorkflows.filter((workflowId: string) =>
            workflowId !== action.payload) : [action.payload, ...state.selectedWorkflows],
        selectedEntities: isSelected ? state.selectedEntities.filter((entity: DataDetails) =>
          entity.type !== 'workflow' || entity.data !== action.payload)
          : [{
            type: 'workflow',
            data: action.payload
          }, ...state.selectedEntities]
      });
    }
    case workflowActions.SHOW_WORKFLOW_VERSIONS: {
      return Object.assign({}, state, {
        openedWorkflow: action.payload,
        selectedWorkflows: [],
        selectedVersions: [],
        selectedVersionsData: [],
        selectedEntities: [],
        selectedGroups: []
      });
    }
    case workflowActions.SELECT_VERSION: {
      const selectedVersions = state.selectedVersions.indexOf(action.payload) > -1 ?
        state.selectedVersions.filter((versionId: string) =>
          versionId !== action.payload) : [action.payload, ...state.selectedVersions];
      return Object.assign({}, state, {
        selectedVersions: selectedVersions,
        selectedVersionsData: state.workflowList.filter((workflow: any) => selectedVersions.indexOf(workflow.id) > -1)
      });
    }
    case workflowActions.SELECT_GROUP: {
      const isSelected = state.selectedGroups.indexOf(action.payload) > -1;
      return Object.assign({}, state, {
        selectedGroups: isSelected ?
          state.selectedGroups.filter((groupName: string) => groupName !== action.payload) :
          [...state.selectedGroups, action.payload],
        selectedEntities: isSelected ? state.selectedEntities.filter((entity: DataDetails) =>
          entity.type !== 'group' || entity.data !== action.payload)
          : [{
            type: 'group',
            data: action.payload
          }, ...state.selectedEntities]
      });
    }
    case workflowActions.DELETE_WORKFLOW_COMPLETE: {
      return Object.assign({}, state, {
        selectedWorkflows: [],
        selectedGroups: [],
        selectedEntities: []
      });
    }
    case workflowActions.DELETE_VERSION_COMPLETE: {
      return Object.assign({}, state, {
        selectedVersions: [],
        selectedVersionsData: []
      });
    }
    case workflowActions.DELETE_GROUP_COMPLETE: {
      return Object.assign({}, state, {
        selectedWorkflows: [],
        selectedGroups: [],
        selectedEntities: []
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
    case workflowActions.CHANGE_VERSIONS_ORDER: {
      return Object.assign({}, state, {
        orderByVersions: action.payload.orderBy,
        sortOrderVersions: action.payload.sortOrder
      });
    }
    case workflowActions.RESET_MODAL: {
      return Object.assign({}, state, {
        showModal: true,
        modalError: ''
      });
    }
    case workflowActions.RENAME_GROUP_COMPLETE: {
      return Object.assign({}, state, {
        showModal: false,
        selectedGroups: [],
        selectedEntities: []
      });
    }
    case workflowActions.CREATE_GROUP_COMPLETE: {
      return Object.assign({}, state, {
        showModal: false,
      });
    }
    case workflowActions.DUPLICATE_WORKFLOW_COMPLETE: {
      return Object.assign({}, state, {
        showModal: false,
      });
    }
    case workflowActions.SAVE_JSON_WORKFLOW_COMPLETE: {
      return Object.assign({}, state, {
        showModal: false,
        reload: true
      });
    }
    case workflowActions.RENAME_WORKFLOW_COMPLETE: {
      return Object.assign({}, state, {
        showModal: false,
        selectedWorkflows: [],
        selectedEntities: []
      });
    }
    case workflowActions.MOVE_WORKFLOW_COMPLETE: {
      return Object.assign({}, state, {
        showModal: false,
        selectedWorkflows: [],
        selectedEntities: []
      });
    }
    case workflowActions.SAVE_JSON_WORKFLOW_ERROR: {
      return Object.assign({}, state, {
        modalError: action.payload
      });
    }
    default:
      return state;
  }
}

export const getSelectedWorkflows: any = (state: State) => state.selectedWorkflows;
export const getSelectedVersion: any = (state: State) => state.selectedVersions.length ?
  state.workflowList.find((workflow: any) => workflow.id === state.selectedVersions[0]) : null;
export const getSearchQuery: any = (state: State) => state.searchQuery;
export const getDisplayOptions: any = (state: State) => state.displayOptions;
export const getSelectedDisplayOption: any = (state: State) => state.selectedDisplayOption;
export const getWorkflowNameValidation: any = (state: State) => state.workflowNameValidation;
export const getReloadState: any = (state: State) => state.reload;
export const getSelectedEntity: any = (state: State) => {
  if (state.selectedEntities.length) {
    const entity = state.selectedEntities[0];
    return entity.type === 'workflow' ? {
      type: 'workflow', data: state.workflowsVersionsList.find((workflow: any) => {
        return workflow.name === entity.data;
      })
    } : {
        type: 'group',
        data: {
          name: state.selectedEntities[0].data
        }
      }
  } else {
    return null;
  }
}
export const getExecutionInfo: any = (state: State) => state.executionInfo;
export const getWorkflowVersions: any = (state: State) => orderBy(Object.assign([], state.openedWorkflow ? state.workflowList.filter((workflow: any) =>
  state.openedWorkflow.name === workflow.name && state.openedWorkflow.group === workflow.group) : []), state.orderByVersions, state.sortOrderVersions);
export const getGroupsList: any = (state: State) => {

  return orderBy(Object.assign([], state.groups.filter((group: any) => {
    if (group.name === state.currentLevel.name) {
      return false;
    } else {
      const split = group.name.split(state.currentLevel.name + FOLDER_SEPARATOR);
      return split.length === 2 && split[0] === '' && split[1].indexOf(FOLDER_SEPARATOR) === -1;
    }
  }).map((group: any) => {
    const split = group.name.split(FOLDER_SEPARATOR);
    return Object.assign({}, group, {
      label: split.length > 1 ? split[split.length - 1] : group.name
    });
  })), state.orderBy, state.sortOrder);
};

export const getWorkFlowList: any = (state: State) => {
  return orderBy(Object.assign([], state.workflowsVersionsList), state.orderBy, state.sortOrder);
};
