/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import * as backupsActions from './../modules/settings/backups/actions/backups';
import * as inputActions from './../modules/templates/actions/input';
import * as outputActions from './../modules/templates/actions/output';
import * as transformationActions from './../modules/templates/actions/transformation';
import * as workflowActions from './../modules/workflows/workflow-managing/actions/workflow-list';
import * as workflowMonitoringActions from './../modules/workflows/workflow-monitoring/actions/workflows';
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
        case backupsActions.EXECUTE_BACKUP_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'EXECUTE_BACKUP_DESCRIPTION'
                }]
            });
        }
        case backupsActions.UPLOAD_BACKUP_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'UPLOAD_BACKUP_DESCRIPTION'
                }]
            });
        }
        case backupsActions.GENERATE_BACKUP_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'GENERATE_BACKUP_DESCRIPTION'
                }]
            });
        }
        case backupsActions.DELETE_BACKUP_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'DELETE_BACKUP_DESCRIPTION'
                }]
            });
        }
        case backupsActions.DELETE_METADATA_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'DELETE_METADATA_DESCRIPTION'
                }]
            });
        }
        case workflowActions.SAVE_JSON_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'CREATED_WORKFLOW'
                }]
            });
        }
        case workflowActions.DUPLICATE_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'DUPLICATED_WORKFLOW'
                }]
            });
        }
        case workflowActions.RUN_WORKFLOW_COMPLETE: {
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
        case workflowActions.STOP_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'STOP_WORKFLOW'
                }]
            });
        }
        case workflowMonitoringActions.RUN_WORKFLOW_COMPLETE: {
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
        case workflowMonitoringActions.STOP_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'STOP_WORKFLOW'
                }]
            });
        }
        case workflowActions.DELETE_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'DELETE_WORKFLOW',

                }]
            })
        }
        case workflowActions.DELETE_GROUP_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'DELETE_GROUP',

                }]
            });
        }
        case workflowActions.DELETE_VERSION_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'DELETE_VERSION',

                }]
            })
        }
        case workflowActions.GENERATE_NEW_VERSION: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'GENERATE_NEW_VERSION',

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
        case workflowActions.LIST_GROUP_WORKFLOWS_FAIL: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.ERROR,
                    title: 'ERROR',
                    description: 'LIST_WORKFLOW_ERROR'
                }]
            });
        }
        case workflowMonitoringActions.LIST_WORKFLOW_FAIL: {
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
        case wizardActions.SAVE_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'WORKFLOW_SAVE_SUCCESS',
                    duration: 1500
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
        default:
            return state;
    }
}

export const getCurrentAlert: any = (state: State) => state.currentAlert;
export const showPersistentError: any = (state: State) => state.showPersistentError;
export const pendingSavedData: any = (state: State) => state.confirmSave;
