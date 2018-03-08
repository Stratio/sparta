/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { InputType } from 'app/models/input.model';
import * as inputActions from './../actions/input';
import { orderBy } from '@utils';

export interface State {
    loaded: boolean;
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
    loaded: false,
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
                inputList: action.payload,
                loaded: true
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
