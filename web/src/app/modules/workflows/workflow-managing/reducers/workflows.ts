/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as workflowActions from '../actions/workflow-list';
import { formatDate } from '@utils';
import { homeGroup } from '@app/shared/constants/global';
import { DataDetails } from './../models/data-details';
import { Group, GroupWorkflow } from './../models/workflows';
import { stat } from 'fs';

export interface State {
   currentLevel: any;
   groups: Array<Group>;
   loading: boolean;
   workflowList: Array<GroupWorkflow>;
   workflowsVersionsList: Array<any>;
   workflowsStatus: any;
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
   showModal: boolean;
   modalError: string;
   saving: boolean;
   notification: {
      text: string;
      status: string;
      autoCloseTime?: number;
      visible: boolean;
   };
   showExecutionConfig: boolean;
   executionContexts: any;
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
   executionInfo: null,
   workflowNameValidation: {
      validatedName: false,
      validatedWorkflow: undefined
   },
   reload: false,
   showModal: true,
   modalError: '',
   saving: false,
   notification: {
      visible: false,
      text: '',
      status: ''
   },
   showExecutionConfig: false,
   executionContexts: null
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
            version.tagsAux = version.tags ? version.tags.join(', ') : '';
            try {
               const lastErrorDate = version.execution.genericDataExecution.lastError.date;
               version.lastErrorDate = formatDate(lastErrorDate, true, true);
            } catch (error) { }
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
         return {
            ...state,
            workflowList: action.payload,
            loading: false,
            workflowsVersionsList: Object.keys(workflows).map(function (key) {
               return workflows[key];
            }),
            selectedVersions: [...state.selectedVersions],
            selectedVersionsData: state.selectedVersions.length ?
               action.payload.filter((version: any) => state.selectedVersions.indexOf(version.id) > -1) : [],
            reload: true
         };
      }
      case workflowActions.LIST_GROUP_WORKFLOWS_FAIL: {
         return {
            ...state,
            loading: false
         };
      }
      case workflowActions.LIST_GROUPS_COMPLETE: {
         return {
            ...state,
            groups: action.payload
         };
      }
      case workflowActions.CHANGE_GROUP_LEVEL: {
         return {
            ...state,
            loading: true
         };
      }
      case workflowActions.CHANGE_GROUP_LEVEL_COMPLETE: {
         return {
            ...state,
            currentLevel: action.payload,
            selectedWorkflows: [],
            openedWorkflow: null,
            workflowList: [],
            workflowsVersionsList: [],
            selectedVersions: [],
            selectedVersionsData: [],
            selectedEntities: [],
            selectedGroups: []
         };
      }
      case workflowActions.REMOVE_WORKFLOW_SELECTION: {
         return {
            ...state,
            selectedWorkflows: [],
            selectedVersions: [],
            selectedVersionsData: [],
            selectedGroups: []
         };
      }
      case workflowActions.SELECT_WORKFLOW: {
         const isSelected = state.selectedWorkflows.indexOf(action.payload) > -1;
         return {
            ...state,
            selectedWorkflows: isSelected ?
               state.selectedWorkflows.filter((workflowId: string) =>
                  workflowId !== action.payload) : [action.payload, ...state.selectedWorkflows],
            selectedEntities: isSelected ? state.selectedEntities.filter((entity: DataDetails) =>
               entity.type !== 'workflow' || entity.data !== action.payload)
               : [{ type: 'workflow', data: action.payload }, ...state.selectedEntities]
         };
      }
      case workflowActions.SHOW_WORKFLOW_VERSIONS: {
         return {
            ...state,
            openedWorkflow: action.payload,
            selectedWorkflows: [],
            selectedVersions: [],
            selectedVersionsData: [],
            selectedEntities: [],
            selectedGroups: []
         };
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
         return {
            ...state,
            selectedGroups: isSelected ?
               state.selectedGroups.filter((groupName: string) => groupName !== action.payload) :
               [...state.selectedGroups, action.payload],
            selectedEntities: isSelected ? state.selectedEntities.filter((entity: DataDetails) =>
               entity.type !== 'group' || entity.data !== action.payload) :
               [{ type: 'group', data: action.payload }, ...state.selectedEntities]
         };
      }
      case workflowActions.DELETE_WORKFLOW_COMPLETE: {
         return {
            ...state,
            selectedWorkflows: [],
            selectedGroups: [],
            selectedEntities: []
         };
      }
      case workflowActions.DELETE_VERSION_COMPLETE: {
         return {
            ...state,
            selectedVersions: [],
            selectedVersionsData: [],
            notification: {
               text: 'DELETE_VERSION',
               status: 'success',
               autoCloseTime: 5000,
               visible: true
            }
         };
      }
      case workflowActions.DELETE_GROUP_COMPLETE: {
         return {
            ...state,
            selectedWorkflows: [],
            selectedGroups: [],
            selectedEntities: []
         };
      }
      case workflowActions.VALIDATE_WORKFLOW_NAME: {
         return {
            ...state,
            workflowNameValidation: {
               validatedName: false,
               validatedWorkflow: action.payload
            }
         };
      }
      case workflowActions.VALIDATE_WORKFLOW_NAME_COMPLETE: {
         return {
            ...state,
            workflowNameValidation: {
               ...state.workflowNameValidation,
               validatedName: true
            }
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
      case workflowActions.RESET_MODAL: {
         return {
            ...state,
            showModal: true,
            modalError: ''
         };
      }
      case workflowActions.RENAME_GROUP_COMPLETE: {
         return {
            ...state,
            showModal: false,
            selectedGroups: [],
            selectedEntities: [],
            notification: {
               text: 'RENAME_GROUP',
               status: 'success',
               autoCloseTime: 5000,
               visible: true
            }
         };
      }
      case workflowActions.CREATE_GROUP_COMPLETE: {
         return {
            ...state,
            showModal: false,
            notification: {
               text: 'CREATE_GROUP',
               status: 'success',
               autoCloseTime: 5000,
               visible: true
            }
         };
      }
      case workflowActions.ADD_GROUP: {
         return {
            ...state,
            groups: [...state.groups, action.payload]
         };
      }

      case workflowActions.DUPLICATE_WORKFLOW_COMPLETE: {
         return {
            ...state,
            showModal: false
         };
      }
      case workflowActions.RUN_WORKFLOW_COMPLETE: {
         return {
            ...state,
            showExecutionConfig: false
         };
      }
      case workflowActions.SAVE_JSON_WORKFLOW: {
         return {
            ...state,
            saving: true
         };
      }
      case workflowActions.SAVE_JSON_WORKFLOW_COMPLETE: {
         return {
            ...state,
            showModal: false,
            reload: true,
            saving: false
         };
      }
      case workflowActions.RENAME_WORKFLOW_COMPLETE: {
         return {
            ...state,
            showModal: false,
            selectedWorkflows: [],
            selectedEntities: [],
            notification: {
               text: 'RENAME_WORKFLOW',
               status: 'success',
               autoCloseTime: 5000,
               visible: true
            }
         };
      }
      case workflowActions.MOVE_WORKFLOW_COMPLETE: {
         return {
            ...state,
            showModal: false,
            selectedWorkflows: [],
            selectedEntities: []
         };
      }
      case workflowActions.SAVE_JSON_WORKFLOW_ERROR: {
         return {
            ...state,
            modalError: action.payload,
            saving: false
         };
      }
      case workflowActions.GENERATE_NEW_VERSION_COMPLETE: {
         const newWorkflow = {
            ...action.payload,
            group: action.payload.group.name,
            nodes: action.payload.pipelineGraph.nodes
         };
         const workflow = { ...state.workflowsVersionsList[0], versions: [...state.workflowsVersionsList[0].versions, newWorkflow] };

         return {
            ...state,
            workflowList: [...state.workflowList, newWorkflow],
            workflowsVersionsList: [workflow]
         };

      }
      case workflowActions.MOVE_WORKFLOW_GROUP: {
         const workflowList = state.workflowList.filter(workflow => workflow.name !== action.payload);
         const workflowsVersionsList = state.workflowsVersionsList.filter(workflow => workflow.name !== action.payload);
         return {
            ...state,
            workflowList,
            workflowsVersionsList,
            notification: {
               text: 'MOVE_WORKFLOW',
               status: 'success',
               autoCloseTime: 5000,
               visible: true
            }
         };
      }

      case workflowActions.SAVE_WORKFLOW_GROUP: {
         const newWorkflow = {
            ...action.payload,
            group: action.payload.group.name,
            nodes: action.payload.pipelineGraph.nodes,
            type: action.payload.executionEngine
         };
         const workflowList = [...state.workflowList, newWorkflow];
         const workflowsVersionsList = [...state.workflowsVersionsList, newWorkflow];

         return {
            ...state,
            workflowList,
            workflowsVersionsList
         };
      }
      case workflowActions.DELETE_WORKFLOW_GROUP: {
         const workflowList = state.workflowList.filter(workflow => action.payload.indexOf(workflow.name) === -1);
         const workflowsVersionsList = state.workflowsVersionsList.filter(workflow => action.payload.indexOf(workflow.name) === -1);
         return {
            ...state,
            workflowList,
            workflowsVersionsList,
            selectedWorkflows: [],
            selectedGroups: [],
            notification: {
               text: 'DELETE_WORKFLOW',
               status: 'success',
               autoCloseTime: 5000,
               visible: true
            }
         };
      }
      case workflowActions.DELETE_SINGLE_VERSION_COMPLETE: {
         return {
            ...state,
            selectedVersions: [],
            selectedVersionsData: [],
            notification: {
               text: 'DELETE_SINGLE_VERSION',
               status: 'success',
               autoCloseTime: 5000,
               visible: true
            }
         };
      }
      case workflowActions.DELETE_GROUP: {
         const groups = state.groups.filter(group => action.payload.indexOf(group.name) === -1);
         return {
            ...state,
            groups
         };
      }
      case workflowActions.DELETE_SINGLE_GROUP_COMPLETE: {
         return {
            ...state,
            groups: state.groups.filter(group => group.id !== action.groupId),
            selectedWorkflows: [],
            selectedGroups: [],
            selectedEntities: [],
            notification: {
               text: 'DELETE_SINGLE_GROUP',
               status: 'success',
               autoCloseTime: 5000,
               visible: true
            }
         };
      }
      case workflowActions.HIDE_NOTIFICATION: {
         return {
            ...state,
            notification: {
               ...state.notification,
               visible: false
            }
         };
      }
      case workflowActions.CONFIG_ADVANCED_EXECUTION_COMPLETE: {
         return {
            ...state,
            showExecutionConfig: true,
            executionContexts: action.config
         };
      }
      case workflowActions.CANCEL_ADVANCED_EXECUTION: {
         return {
            ...state,
            showExecutionConfig: false
         };
      }
      default:
         return state;
   }
}



export const getSelectedWorkflows: any = (state: State) => state.selectedWorkflows;
export const getSelectedVersion: any = (state: State) => state.selectedVersions.length ?
   state.workflowList.find((workflow: any) => workflow.id === state.selectedVersions[0]) : null;
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
};
export const getExecutionInfo: any = (state: State) => state.executionInfo;
