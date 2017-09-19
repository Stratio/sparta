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
import { inputNames } from 'data-templates/inputs';
import { outputNames } from 'data-templates/outputs';

export interface State {
    nodes: Array<any>;
    edges: Array<any>;
    selectedCreationEntity: any;
    entityCreationMode: boolean;
    menuOptions: Array<FloatingMenuModel>;
    selectedEntity: string;
    showEntityDetails: boolean;
};

const initialState: State = {
    edges: [
        {
            origin: "kafka",
            destination: "Postgress"
        },
        {
            origin: "kafka",
            destination: "Join2"
        }
    ],
    nodes: [{
        type: 'input',
        name: 'kafka',
        position: {
            x: 0,
            y: 0
        }
    },
    {
        type: 'output',
        name: 'Join2',
        position: {
            x: 180,
            y: -127
        }
    },
    {
        type: 'output',
        name: 'Postgress',
        position: {
            x: 180,
            y: 140
        }
    },
    {
        type: 'output',
        name: 'Join',
        position: {
            x: 500,
            y: 7
        }
    }],
    selectedCreationEntity: null,
    entityCreationMode: false,
    selectedEntity:'',
    showEntityDetails: false,
    menuOptions: [{
        name: 'Input',
        icon: 'icon-login',
        value: 'action',
        subMenus: [...[{
            name: 'Templates',
            value: '',
            subMenus: []
        }], ...inputNames]
    },
    {
        name: 'Transformation',
        value: 'action',
        icon: 'icon-shuffle',
        subMenus: [...[{
            name: 'Templates',
            value: '',
            subMenus: []
        }], ...inputNames]
    },
    {
        name: 'Output',
        value: 'action',
        icon: 'icon-logout',
        subMenus: [...[{
            name: 'Templates',
            value: '',
            subMenus: []
        }], ...outputNames]
    }]
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case wizardActions.actionTypes.SELECTED_CREATION_ENTITY: {
            return Object.assign({}, state, {
                selectedCreationEntity: action.payload,
                entityCreationMode: true
            });
        }
        case wizardActions.actionTypes.DESELECTED_CREATION_ENTITY: {
            return Object.assign({}, state, {
                selectedCreationEntity: null,
                entityCreationMode: false
            });
        }
        case wizardActions.actionTypes.SELECT_ENTITY: {
            return Object.assign({}, state, {
                selectedEntity: action.payload
            })
        }
        case wizardActions.actionTypes.UNSELECT_ENTITY: {
            return Object.assign({}, state, {
                selectedEntity: ''
            })
        }
        case wizardActions.actionTypes.TOGGLE_ENTITY_DETAILS: {
            return Object.assign({}, state, {
                showEntityDetails: !state.showEntityDetails
            })
        }
        case wizardActions.actionTypes.CREATE_NODE_RELATION: {
            return Object.assign({}, state, {
                edges: [...state.edges, action.payload]
            })
        }
        default:
            return state;
    }
}

export const getMenuOptions: any = (state: State) => state.menuOptions;
export const getSelectedEntities: any = (state: State) => state.selectedEntity;
export const isShowedEntityDetails: any = (state: State) => state.showEntityDetails;
export const getWorkflowRelations: any = (state: State) => state.edges;
export const getWorkflowNodes: any = (state: State) => state.nodes;
export const isCreationMode: any = (state: State) => {
    return {
        active: state.entityCreationMode,
        data: state.selectedCreationEntity
    };
};


