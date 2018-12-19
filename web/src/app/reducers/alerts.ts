/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import * as inputActions from './../modules/templates/actions/input';
import * as outputActions from './../modules/templates/actions/output';
import * as transformationActions from './../modules/templates/actions/transformation';
import * as repositoryActions from './../modules/repository/actions/workflow-list';
import * as executionsActions from './../modules/executions/executions-managing/actions/executions';

import * as wizardActions from './../modules/wizard/actions/wizard';
import * as errorsActions from 'actions/errors';
import * as environmentActions from './../modules/settings/environment/actions/environment';

import { CustomAlert } from 'app/models/alert.model';
import { STALERT_SEVERITY } from '@stratio/egeo';

export interface State {
   confirmSave: boolean;
   currentAlert: Array<CustomAlert>;
   notification: any;
   showPersistentError: boolean;
}

const initialState: State = {
   confirmSave: false,
   currentAlert: [],
   notification: null,
   showPersistentError: false
};

export function reducer(state: State = initialState, action: any): State {
   switch (action.type) {
      case errorsActions.SAVED_DATA_NOTIFICATION: {
         return Object.assign({}, state, {
            confirmSave: true
         });
      }
      case errorsActions.HIDE_SAVED_DATA_NOTIFICATION: {
         return Object.assign({}, state, {
            confirmSave: false
         });
      }
      case errorsActions.CHANGE_ROUTE: {
         return Object.assign({}, state, {
            showPersistentError: false
         });
      }
      case errorsActions.FORBIDDEN_ERROR: {
         return Object.assign({}, state, {
            showPersistentError: true
         });
      }
      case errorsActions.HTTP_ERROR: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               title: 'ERROR',
               description: 'EXPIRED_SESSION'
            }]
         });
      }
      case inputActions.UPDATE_INPUT_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'UPDATE_INPUT_DESCRIPTION'
            }]
         });
      }
      case transformationActions.UPDATE_TRANSFORMATION_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'UPDATE_TRANSFORMATION_DESCRIPTION'
            }]
         });
      }
      case inputActions.DELETE_INPUT_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'DELETE_INPUT_DESCRIPTION'
            }]
         });
      }
      case outputActions.DELETE_OUTPUT_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'DELETE_OUTPUT_DESCRIPTION'
            }]
         });
      }
      case transformationActions.DELETE_TRANSFORMATION_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'DELETE_TRANSFORMATION_DESCRIPTION'
            }]
         });
      }
      case outputActions.UPDATE_OUTPUT_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'UPDATE_OUTPUT_DESCRIPTION'
            }]
         });
      }
      case repositoryActions.DUPLICATE_WORKFLOW_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'DUPLICATED_WORKFLOW'
            }]
         });
      }
      case repositoryActions.RUN_WORKFLOW_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'RUN_WORKFLOW',
               params: {
                  name: action.payload
               }
            }]
         });
      }
      case executionsActions.RERUN_EXECUTION_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'RUN_WORKFLOW',
               params: {
                  name: action.payload
               }
            }]
         });
      }
      case repositoryActions.RUN_WORKFLOW_ERROR: {
         let message = '';
         try {
            const error = JSON.parse(action.payload.error);
            message = error.exception;
         } catch (error) {
            message = action.payload.message;
         }
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               title: 'ERROR',
               text: message,
               duration: 4000
            }]
         });
      }
      case executionsActions.RERUN_EXECUTION_ERROR: {
        let message = '';
         try {
            const error = JSON.parse(action.payload.error);
            message = error.exception;
         } catch (error) {
            message = action.payload.message;
         }
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               title: 'ERROR',
               text: message,
               duration: 4000
            }]
         });
      }
      case repositoryActions.RUN_WORKFLOW_VALIDATION_ERROR: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               title: 'ERROR',
               text: action.payload.map(error => error.message).join('.    '),
               duration: 4000
            }]
         });
      }
      case repositoryActions.DELETE_WORKFLOW_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'DELETE_WORKFLOW',

            }]
         });
      }
      case repositoryActions.DELETE_GROUP_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'DELETE_GROUP',

            }]
         });
      }
      case inputActions.LIST_INPUT_FAIL: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               title: 'ERROR',
               description: 'LIST_INPUT_ERROR'
            }]
         });
      }
      case repositoryActions.LIST_GROUP_WORKFLOWS_FAIL: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               title: 'ERROR',
               description: 'LIST_WORKFLOW_ERROR'
            }]
         });
      }
      case outputActions.LIST_OUTPUT_FAIL: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               title: 'ERROR',
               description: 'LIST_OUTPUT_ERROR'
            }]
         });
      }
      case transformationActions.LIST_TRANSFORMATION_FAIL: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               title: 'ERROR',
               description: 'LIST_TRANSFORMATION_ERROR'
            }]
         });
      }
      case wizardActions.GET_MENU_TEMPLATES_ERROR: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               title: 'ERROR',
               description: 'MENU_TEMPLATES_ERROR',
               duration: 1500
            }]
         });
      }
      case inputActions.CREATE_INPUT_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'CREATE_INPUT_DESCRIPTION'
            }]
         });
      }
      case outputActions.CREATE_OUTPUT_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'CREATE_OUTPUT_DESCRIPTION'
            }]
         });
      }
      case transformationActions.CREATE_TRANSFORMATION_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'CREATE_TRANSFORMATION_DESCRIPTION'
            }]
         });
      }
      case errorsActions.SERVER_ERROR_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               notranslate: true,
               title: action.payload.title,
               description: action.payload.description,
               duration: 5000
            }]
         });
      }
      case wizardActions.SAVE_WORKFLOW_ERROR: {
         return Object.assign({}, state, {
            currentAlert: action.payload.title ? [{
               type: STALERT_SEVERITY.ERROR,
               title: action.payload.title ? action.payload.title : 'ERROR',
               description: action.payload.description ? action.payload.description : (action.payload.exception ? action.payload.exception : 'SERVER_ERROR')
            }] : []
         });
      }
      case environmentActions.SAVE_ENVIRONMENT: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'SAVE_ENVIRONMENT'
            }]
         });
      }
      case environmentActions.EXPORT_ENVIRONMENT_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'EXPORT_ENVIRONMENT'
            }]
         });
      }
      case environmentActions.IMPORT_ENVIRONMENT_COMPLETE: {
         return Object.assign({}, state, {
            currentAlert: [{
               type: STALERT_SEVERITY.SUCCESS,
               title: 'SUCCESS',
               description: 'IMPORT_ENVIRONMENT'
            }]
         });
      }
      case environmentActions.INVALID_FILE_ERROR: {
         return {
            ...state,
            currentAlert: [{
               type: STALERT_SEVERITY.ERROR,
               title: 'ERROR',
               description: 'INVALID_ENVIRONMENT_FILE'
            }]
         };
      }
      default:
         return state;
   }
}

export const getCurrentAlert: any = (state: State) => state.currentAlert;
export const showPersistentError: any = (state: State) => state.showPersistentError;
export const pendingSavedData: any = (state: State) => state.confirmSave;
