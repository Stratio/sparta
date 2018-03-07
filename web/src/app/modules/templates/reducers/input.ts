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

import { InputType } from 'app/models/input.model';
import * as inputActions from './../actions/input';
import { orderBy } from '@utils';

export interface State {
    inputList: Array<InputType>;
    selectedDisplayOption: string;
    editedInput: any;
    sortOrder: boolean;
    orderBy: string;
    selectedInputs: Array<InputType>;
    selectedInputsIds: Array<string>;
    isSaved: boolean;
}

const initialState: State = {
    inputList: [],
    selectedDisplayOption: 'BLOCKS',
    editedInput: {},
    selectedInputs: [],
    sortOrder: true,
    orderBy: 'name',
    selectedInputsIds: [],
    isSaved: false
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case inputActions.LIST_INPUT: {
            return Object.assign({}, state, {});

        }
        case inputActions.LIST_INPUT_COMPLETE: {
            return Object.assign({}, state, {
                inputList: action.payload
            });

        }
        case inputActions.GET_EDITED_INPUT_COMPLETE: {
            return Object.assign({}, state, {
                editedInput: action.payload
            });
        }
        case inputActions.SELECT_INPUT: {
            return Object.assign({}, state, {
                selectedInputs: [...state.selectedInputs, action.payload],
                selectedInputsIds: [...state.selectedInputsIds, action.payload.id]
            });
        }
        case inputActions.DESELECT_INPUT: {
            const newSelection = state.selectedInputs.filter((input: any) => {
                if (input.id !== action.payload.id) {
                    return input;
                }
            });
            return Object.assign({}, state, {
                selectedInputs: newSelection,
                selectedInputsIds: newSelection.map((input) => {
                    return input.id;
                })
            });
        }
        case inputActions.DELETE_INPUT_COMPLETE: {
            const inputId = action.payload;
            return Object.assign({}, state, {
                selectedInputs: [],
                selectedInputsIds: []
            });
        }
        case inputActions.DISPLAY_MODE: {
            return Object.assign({}, state, {
                selectedDisplayOption: state.selectedDisplayOption === 'BLOCKS' ? 'ROWS' : 'BLOCKS'
            });
        }
        case inputActions.EDIT_INPUT: {
            return Object.assign({}, state, {
                editedInput: action.payload
            });
        }
        case inputActions.UPDATE_INPUT_COMPLETE: {
            return Object.assign({}, state, {
                isSaved: true
            });
        }
        case inputActions.CREATE_INPUT_COMPLETE: {
            return Object.assign({}, state, {
                isSaved: true
            });
        }
        case inputActions.RESET_INPUT_FORM: {
            return Object.assign({}, state, {
                isSaved: false,
                selectedInputs: [],
                selectedInputsIds: [],
                editedInput: {},
                sortOrder: true,
                orderBy: 'name',
            });
        }
        case inputActions.CHANGE_ORDER: {
            return Object.assign({}, state, {
                orderBy: action.payload.orderBy,
                sortOrder: action.payload.sortOrder,
                selectedInputs: [],
                selectedInputsIds: []
            });
        }
        default:
            return state;
    }
}

export const getInputList: any = (state: State) => {
    return orderBy(Object.assign([], state.inputList), state.orderBy, state.sortOrder);
};
export const getSelectedInputs: any = (state: State) => {
    return {
        selected: state.selectedInputs,
        selectedIds: state.selectedInputsIds
    };
};
export const isInputSaved: any = (state: State) => state.isSaved;
export const getSelectedDisplayOption: any = (state: State) => state.selectedDisplayOption;
export const getEditedInput: any = (state: State) => state.editedInput;
