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

import { BackupType } from 'app/models/backup.model';

import * as backupsActions from 'actions/backups';
import * as inputActions from 'actions/input';
import * as outputActions from 'actions/output';
import * as workflowActions from 'actions/workflow';
import * as wizardActions from 'actions/wizard';
import * as errorsActions from 'actions/errors';

import { CustomAlert } from 'app/models/alert.model';
import { STALERT_SEVERITY } from '@stratio/egeo';

export interface State {
    currentAlert: Array<CustomAlert>;
    notification: any;
    showPersistentError: boolean;
}

const initialState: State = {
    currentAlert: [],
    notification: null,
    showPersistentError: false
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
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
        case outputActions.UPDATE_OUTPUT_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'UPDATE_OUTPUT_DESCRIPTION'
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
        case workflowActions.SAVE_JSON_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'CREATED_WORKFLOW'
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
        case workflowActions.DELETE_WORKFLOW_COMPLETE: {
            let names = '';
            const workflows = action.payload;
            for (let i = 0; i < workflows.length; i++) {
                names += workflows[i].name;
                if (i < workflows.length) {
                    names += ', ';
                }
            };
            return Object.assign({}, state, {
                currentAlert: action.payload.map((workflow: any) => {
                    return {
                        type: STALERT_SEVERITY.SUCCESS,
                        title: 'SUCCESS',
                        description: 'DELETE_WORKFLOW',
                        params: {
                            name: names
                        }
                    };
                })
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
        case workflowActions.LIST_WORKFLOW_FAIL: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.ERROR,
                    title: 'ERROR',
                    description: 'LIST_WORKFLOW_ERROR'
                }]
            });
        }
        case wizardActions.SAVE_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.SUCCESS,
                    title: 'SUCCESS',
                    description: 'WORKFLOW_SAVE_SUCCESS'
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
        case errorsActions.SERVER_ERROR: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.ERROR,
                    notranslate: true,
                    title: action.payload.title,
                    description: action.payload.description,
                    duration: 1500
                }]
            });
        }
        case wizardActions.SAVE_WORKFLOW_ERROR: {
            return Object.assign({}, state, {
                currentAlert: [{
                    type: STALERT_SEVERITY.ERROR,
                    title: action.payload.title ? action.payload.title : 'ERROR',
                    description: action.payload.description ? action.payload.description : 'SERVER_ERROR'
                }]
            });
        }
        default:
            return state;
    }
}

export const getCurrentAlert: any = (state: State) => state.currentAlert;
export const showPersistentError: any = (state: State) => state.showPersistentError;
