/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { OutputType } from 'app/models/output.model';
import * as outputActions from './../actions/output';
import { orderBy } from '@utils';

export interface State {
    loaded: boolean;
    outputList: Array<OutputType>;
    selectedDisplayOption: string;
    editedOutput: any;
    sortOrder: boolean;
    orderBy: string;
    selectedOutputs: Array<OutputType>;
    selectedOutputsIds: Array<string>;
    isSaved: boolean;
};

const initialState: State = {
    loaded: false,
    outputList: [],
    selectedDisplayOption: 'BLOCKS',
    editedOutput: {},
    sortOrder: true,
    orderBy: 'name',
    selectedOutputs: [],
    selectedOutputsIds: [],
    isSaved: false
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case outputActions.LIST_OUTPUT: {
            return Object.assign({}, state, {});

        }
        case outputActions.LIST_OUTPUT_COMPLETE: {
            return Object.assign({}, state, {
                outputList: action.payload,
                loaded: true
            });

        }
        case outputActions.GET_EDITED_OUTPUT_COMPLETE: {
            return Object.assign({}, state, {
                editedOutput: action.payload
            });
        }
        case outputActions.SELECT_OUTPUT: {
            return Object.assign({}, state, {
                selectedOutputs: [...state.selectedOutputs, action.payload],
                selectedOutputsIds: [...state.selectedOutputsIds, action.payload.id]
            });
        }
        case outputActions.DESELECT_OUTPUT: {
            const newSelection = state.selectedOutputs.filter((output: any) => {
                if (output.id !== action.payload.id) {
                    return output;
                }
            });
            return Object.assign({}, state, {
                selectedOutputs: newSelection,
                selectedOutputsIds: newSelection.map((output) => {
                    return output.id;
                })
            });
        }
        case outputActions.DELETE_OUTPUT_COMPLETE: {
            const outputId = action.payload;
            return Object.assign({}, state, {
                selectedOutputs: [],
                selectedOutputsIds: []
            });
        }
        case outputActions.DISPLAY_MODE: {
            return Object.assign({}, state, {
                selectedDisplayOption: state.selectedDisplayOption === 'BLOCKS' ? 'ROWS' : 'BLOCKS'
            });
        }
        case outputActions.EDIT_OUTPUT: {
            return Object.assign({}, state, {
                editedOutput: action.payload
            });
        }
        case outputActions.UPDATE_OUTPUT_COMPLETE: {
            return Object.assign({}, state, {
                isSaved: true
            });
        }
        case outputActions.CREATE_OUTPUT_COMPLETE: {
            return Object.assign({}, state, {
                isSaved: true
            });
        }
        case outputActions.RESET_OUTPUT_FORM: {
            return Object.assign({}, state, {
                isSaved: false,
                selectedOutputs: [],
                selectedOutputsIds: [],
                sortOrder: true,
                orderBy: 'name'
            });
        }
        case outputActions.CHANGE_ORDER: {
            return Object.assign({}, state, {
                orderBy: action.payload.orderBy,
                sortOrder: action.payload.sortOrder,
                selectedOutputs: [],
                selectedOutputsIds: []
            });
        }
        default:
            return state;
    }
}

export const getOutputList: any = (state: State) => {
    return orderBy(Object.assign([], state.outputList), state.orderBy, state.sortOrder);
};
export const getSelectedOutputs: any = (state: State) => {
    return {
        selected: state.selectedOutputs,
        selectedIds: state.selectedOutputsIds
    };
};
export const getSelectedDisplayOption: any = (state: State) => state.selectedDisplayOption;
export const getEditedOutput: any = (state: State) => state.editedOutput;
export const isOutputSaved: any = (state: State) => state.isSaved;

