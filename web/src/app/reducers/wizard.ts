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

import * as wizardActions from 'actions/wizard';
import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';
import { streamingInputsNames, batchInputsNames } from 'data-templates/inputs';
import { streamingTransformationsNames, batchTransformationsNames } from 'data-templates/transformations';
import { streamingOutputsNames, batchOutputsNames } from 'data-templates/outputs';
import { settingsTemplate } from 'data-templates/index';
import { InitializeSchemaService } from 'app/services';

export interface State {
    editionMode: boolean;
    workflowId: string;
    workflowGroup: string;
    workflowVersion: string;
    workflowType: string;
    loading: boolean;
    nodes: Array<any>;
    edges: Array<any>;
    redoStates: any;
    undoStates: any;
    pristineWorkflow: boolean;
    savedWorkflow: boolean;
    validationErrors: any;
    selectedCreationEntity: any;
    entityNameValidation: boolean;
    entityCreationMode: boolean;
    showSettings: boolean;
    editionConfig: boolean;
    editionConfigType: string;
    editionConfigData: any;
    editionSaved: boolean;
    menuOptions: Array<FloatingMenuModel>;
    selectedEntity: string;
    showEntityDetails: boolean;
    selectedRelation: any;
    svgPosition: any;
    settings: any;
    floatingMenuSearch: string;
};

const defaultSettings = InitializeSchemaService.setDefaultWorkflowSettings(settingsTemplate);

const initialState: State = {
    editionMode: false,
    workflowId: '',
    workflowGroup: '',
    workflowType: '',
    workflowVersion: '0',
    loading: true,
    settings: Object.assign({}, defaultSettings),
    svgPosition: {
        x: 0,
        y: 0,
        k: 1
    },
    edges: [],
    redoStates: [],
    undoStates: [],
    pristineWorkflow: true,
    nodes: [],
    validationErrors: {},
    savedWorkflow: false,
    editionConfig: false,
    editionConfigType: '',
    editionConfigData: null,
    editionSaved: false,
    selectedCreationEntity: null,
    entityNameValidation: false,
    entityCreationMode: false,
    selectedRelation: null,
    selectedEntity: '',
    showEntityDetails: false,
    showSettings: false,
    floatingMenuSearch: '',
    menuOptions: [{
        name: 'Input',
        icon: 'icon-login',
        value: 'action',
        subMenus: [...[{
            name: 'Templates',
            value: '',
            subMenus: []
        }]]
    },
    {
        name: 'Transformation',
        value: 'action',
        icon: 'icon-shuffle',
        subMenus: [...[{
            name: 'Templates',
            value: '',
            subMenus: []
        }]]
    },
    {
        name: 'Output',
        value: 'action',
        icon: 'icon-logout',
        subMenus: [...[{
            name: 'Templates',
            value: '',
            subMenus: []
        }]]
    }]
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case wizardActions.RESET_WIZARD: {
            return action.payload ? {
                ...initialState,
                settings: {
                    ...initialState.settings,
                    basic: {
                        ...initialState.settings.basic,
                        name: ''
                    }
                }
            } : initialState;
        }
        case wizardActions.SELECTED_CREATION_ENTITY: {
            return {
                ...state,
                selectedCreationEntity: action.payload,
                entityNameValidation: false,
                entityCreationMode: true
            };
        }
        case wizardActions.DESELECTED_CREATION_ENTITY: {
            return Object.assign({}, state, {
                selectedCreationEntity: null,
                entityCreationMode: false
            });
        }
        case wizardActions.SAVE_ENTITY_ERROR: {
            return Object.assign({}, state, {
                entityNameValidation: action.payload
            });
        }
        case wizardActions.SELECT_ENTITY: {
            return Object.assign({}, state, {
                selectedEntity: action.payload
            });
        }
        case wizardActions.UNSELECT_ENTITY: {
            return Object.assign({}, state, {
                selectedEntity: ''
            });
        }
        case wizardActions.TOGGLE_ENTITY_DETAILS: {
            return Object.assign({}, state, {
                showEntityDetails: !state.showEntityDetails
            });
        }
        case wizardActions.CREATE_NODE_RELATION_COMPLETE: {
            return Object.assign({}, state, {
                edges: [...state.edges, action.payload],
                undoStates: getUndoState(state),
                pristineWorkflow: false,
                redoStates: []
            });
        }
        case wizardActions.DELETE_NODE_RELATION: {
            return Object.assign({}, state, {
                edges: state.edges.filter((edge: any) => {
                    return edge.origin !== action.payload.origin || edge.destination !== action.payload.destination;
                }),
                undoStates: getUndoState(state),
                pristineWorkflow: false,
                selectedRelation: null,
                redoStates: []
            });
        }
        case wizardActions.DELETE_ENTITY: {
            return Object.assign({}, state, {
                selectedEntity: '',
                nodes: state.nodes.filter((node: any) => {
                    return state.selectedEntity !== node.name;
                }),
                pristineWorkflow: false,
                edges: state.edges.filter((edge: any) => {
                    return state.selectedEntity !== edge.origin && state.selectedEntity !== edge.destination;
                }),
                undoStates: getUndoState(state),
                redoStates: []
            });
        }
        case wizardActions.SHOW_EDITOR_CONFIG: {
            return Object.assign({}, state, {
                editionConfig: true,
                editionConfigType: action.payload,
                editionSaved: false,
                selectedEntity: ''
            });
        }
        case wizardActions.HIDE_EDITOR_CONFIG: {
            return Object.assign({}, state, {
                editionConfig: false
            });
        }
        case wizardActions.CREATE_ENTITY: {
            return Object.assign({}, state, {
                nodes: [...state.nodes, action.payload],
                undoStates: getUndoState(state),
                pristineWorkflow: false,
                redoStates: []
            });
        }
        case wizardActions.DUPLICATE_NODE: {
            return Object.assign({}, state, {
                selectedCreationEntity: action.payload,
                entityCreationMode: true
            });
        }
        case wizardActions.SAVE_WORKFLOW_POSITIONS: {
            return Object.assign({}, state, {
                nodes: action.payload
            });
        }
        case wizardActions.SAVE_EDITOR_POSITION: {
            return Object.assign({}, state, {
                svgPosition: action.payload
            });
        }
        case wizardActions.SAVE_ENTITY_COMPLETE: {
            return Object.assign({}, state, {
                pristineWorkflow: false,
                nodes: state.nodes.map((node: any) => {
                    return node.name === action.payload.oldName ? action.payload.data : node;
                }),
                edges: state.edges.map((edge: any) => {
                    if (edge.origin === action.payload.oldName) {
                        return {
                            origin: action.payload.data.name,
                            destination: edge.destination
                        };
                    } else if (edge.destination === action.payload.oldName) {
                        return {
                            origin: edge.origin,
                            destination: action.payload.data.name
                        };
                    } else {
                        return edge;
                    }
                }),
                undoStates: getUndoState(state),
                redoStates: [],
                editionSaved: true
            });
        }
        case wizardActions.SAVE_SETTINGS: {
            return Object.assign({}, state, {
                pristineWorkflow: false,
                settings: action.payload
            });
        }
        case wizardActions.EDIT_ENTITY: {
            const findEntity = state.nodes.find((node) => {
                return node.name === state.selectedEntity;
            });
            return Object.assign({}, state, {
                editionConfig: true,
                editionConfigType: {
                    stepType: findEntity.stepType,
                    data: findEntity
                },
                editionSaved: false
            });
        }
        case wizardActions.CHANGE_WORKFLOW_NAME: {
            return {
                ...state,
                pristineWorkflow: false,
                settings: {
                    ...state.settings,
                    basic: {
                        ...state.settings.basic,
                        name: action.payload
                    }
                }
            };
        }
        case wizardActions.MODIFY_WORKFLOW_COMPLETE: {
            const workflow = action.payload;
            return Object.assign({}, state, {
                loading: false,
                svgPosition: workflow.uiSettings.position,
                nodes: workflow.pipelineGraph.nodes,
                edges: workflow.pipelineGraph.edges,
                workflowId: workflow.id,
                workflowGroup: workflow.group,
                workflowVersion: workflow.version,
                settings: {
                    basic: {
                        name: workflow.name,
                        description: workflow.description,
                        tags: workflow.tags
                    },
                    advancedSettings: workflow.settings
                }
            });
        }
        case wizardActions.SAVE_WORKFLOW: {
            return Object.assign({}, state, {
                loading: true
            });
        }
        case wizardActions.SAVE_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                savedWorkflow: true,
                pristineWorkflow: true,
                loading: false
            });
        }
        case wizardActions.SAVE_WORKFLOW_ERROR: {
            return Object.assign({}, state, {
                loading: false
            });
        }
        case wizardActions.GET_MENU_TEMPLATES_COMPLETE: {
            const menuOptions: any = JSON.parse(JSON.stringify(state.menuOptions));
            menuOptions[0].subMenus[0].subMenus = action.payload.input.filter((input: any) =>
                input.executionEngine === state.workflowType)
                .map((template: any) => {
                    return {
                        name: template.name,
                        type: 'template',
                        data: template,
                        stepType: 'Input'
                    };
                });
            menuOptions[2].subMenus[0].subMenus = action.payload.output.filter((output: any) =>
                output.executionEngine === state.workflowType)
                .map((template: any) => {
                    return {
                        name: template.name,
                        type: 'template',
                        data: template,
                        stepType: 'Output'
                    };
                });
            menuOptions[1].subMenus[0].subMenus = action.payload.transformation.filter((transformation: any) =>
                transformation.executionEngine === state.workflowType)
                .map((template: any) => {
                    return {
                        name: template.name,
                        type: 'template',
                        data: template,
                        stepType: 'Transformation'
                    };
                });
            return Object.assign({}, state, {
                menuOptions: menuOptions
            });
        }
        case wizardActions.SAVE_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                savedWorkflow: true
            });
        }
        case wizardActions.SELECT_SEGMENT: {
            return Object.assign({}, state, {
                selectedRelation: action.payload
            });
        }
        case wizardActions.UNSELECT_SEGMENT: {
            return Object.assign({}, state, {
                selectedRelation: null
            });
        }
        case wizardActions.SEARCH_MENU_OPTION: {
            return Object.assign({}, state, {
                floatingMenuSearch: action.payload
            });
        }
        case wizardActions.UNDO_CHANGES: {
            if (state.undoStates.length) {
                const undoState = state.undoStates[0];
                return Object.assign({}, state, {
                    nodes: JSON.parse(JSON.stringify(undoState.nodes)),
                    edges: JSON.parse(JSON.stringify(undoState.edges)),
                    redoStates: getRedoState(state),
                    undoStates: state.undoStates.slice(1)
                });
            } else {
                return Object.assign({}, state);
            }
        }
        case wizardActions.REDO_CHANGES: {
            if (state.redoStates.length) {
                const redoState = state.redoStates[0];
                return Object.assign({}, state, {
                    nodes: JSON.parse(JSON.stringify(redoState.nodes)),
                    edges: JSON.parse(JSON.stringify(redoState.edges)),
                    undoStates: getUndoState(state),
                    redoStates: state.redoStates.slice(1)
                });
            } else {
                return Object.assign({}, state);
            }
        }
        case wizardActions.VALIDATE_WORKFLOW_COMPLETE: {
            return Object.assign({}, state, {
                validationErrors: action.payload
            });
        }
        case wizardActions.SET_WORKFLOW_TYPE: {
            const menuOptions = JSON.parse(JSON.stringify(state.menuOptions));
            return Object.assign({}, state, {
                workflowType: action.payload,
                loading: false,
                menuOptions: menuOptions.map((option: any) => {
                    switch (option.name) {
                        case 'Input':
                            option.subMenus = option.subMenus.concat(action.payload === 'Streaming' ?
                                streamingInputsNames : batchInputsNames);
                            return option;
                        case 'Output':
                            option.subMenus = option.subMenus.concat(action.payload === 'Streaming' ?
                                streamingOutputsNames : batchOutputsNames);
                            return option;
                        case 'Transformation':
                            option.subMenus = option.subMenus.concat(action.payload === 'Streaming' ?
                                streamingTransformationsNames : batchTransformationsNames);
                            return option;
                    }
                })
            });
        }
        case wizardActions.SET_WIZARD_DIRTY: {
            return Object.assign({}, state, {
                pristineWorkflow: false
            });
        }
        case wizardActions.SHOW_SETTINGS: {
            return Object.assign({}, state, {
                showSettings: true
            });
        }
        case wizardActions.HIDE_SETTINGS: {
            return Object.assign({}, state, {
                showSettings: false
            });
        }
        default:
            return state;
    }
}

function getUndoState(state: any) {
    const undoState: any = {
        nodes: JSON.parse(JSON.stringify(state.nodes)),
        edges: JSON.parse(JSON.stringify(state.edges))
    };
    return [undoState, ...state.undoStates.filter((value: any, index: number) => {
        return index < 4;
    })];
}

function getRedoState(state: any) {
    const redoState: any = {
        nodes: JSON.parse(JSON.stringify(state.nodes)),
        edges: JSON.parse(JSON.stringify(state.edges))
    };
    return [redoState, ...state.redoStates.filter((value: any, index: number) => {
        return index < 4;
    })];
}

export const getMenuOptions: any = (state: State) => {
    // floatingMenuSearch state.menuOptions;
    if (state.floatingMenuSearch.length) {
        let menu: any = [];
        const matchString = state.floatingMenuSearch.toLowerCase();
        state.menuOptions.forEach((option: any) => {
            const icon = option.icon;
            const options: any = [];
            option.subMenus.forEach((type: any) => {
                if (!type.subMenus && type.name.toLowerCase().indexOf(matchString) != -1) {
                    options.push(Object.assign({}, type, {
                        icon: icon
                    }));
                }
            });
            menu = menu.concat(options);
        });
        return menu;
    } else {
        return state.menuOptions;
    }
};

export const getSelectedEntities: any = (state: State) => state.selectedEntity;
export const getSelectedEntityData: any = (state: State) => state.nodes.find((node: any) => node.name === state.selectedEntity);
export const isPristine: any = (state: State) => state.pristineWorkflow;
export const isLoading: any = (state: State) => state.loading;
export const isShowedEntityDetails: any = (state: State) => state.showEntityDetails;
export const showSettings: any = (state: State) => state.showSettings;
export const getWorkflowRelations: any = (state: State) => state.edges;
export const getWorkflowNodes: any = (state: State) => state.nodes;
export const isEntitySaved: any = (state: State) => state.editionSaved;
export const getWorkflowSettings: any = (state: State) => state.settings;
export const getWorkflowName: any = (state: State) => state.settings.basic.name;
export const getWorkflowHeaderData: any = (state: State) => {
    return {
        name: state.settings.basic.name,
        version: state.workflowVersion
    };
};
export const getWorkflowPosition: any = (state: State) => state.svgPosition;
export const isSavedWorkflow: any = (state: State) => state.savedWorkflow;
export const getValidationErrors: any = (state: State) => state.validationErrors;
export const areUndoRedoEnabled: any = (state: State) => {
    return {
        undo: state.undoStates.length ? true : false,
        redo: state.redoStates.length ? true : false
    };
};
export const getSelectedRelation: any = (state: State) => state.selectedRelation;
export const getWorkflowType: any = (state: State) => state.workflowType;
export const getEditionConfigMode: any = (state: State) => {
    return {
        isEdition: state.editionConfig,
        editionType: state.editionConfigType
    };
};
export const isCreationMode: any = (state: State) => {
    return {
        active: state.entityCreationMode,
        data: state.selectedCreationEntity
    };
};
export const getValidatedEntityName: any = (state: State) => state.entityNameValidation;
export const getErrorsManagementOutputs: any = (state: State) => state.nodes.reduce((filtered: Array<string>, workflowNode: any) => {
    if (workflowNode.stepType === 'Output' && workflowNode.configuration.errorSink) {
        filtered.push(workflowNode.name);
    }
    return filtered;
}, []);
