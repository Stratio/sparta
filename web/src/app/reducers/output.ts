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

import { OutputType } from 'app/models/output.model';
import * as outputActions from 'actions/output';

export interface State {
    outputList: Array<OutputType>;
    selectedDisplayOption: string;
    editedOutput: any;
    selectedOutputs: Array<OutputType>;
    selectedOutputsIds: Array<string>;
    isSaved: boolean;
};

const initialState: State = {
    outputList: [],
    selectedDisplayOption: 'BLOCKS',
    editedOutput: {},
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
                outputList: action.payload
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
                selectedOutputsIds: []
            });
        }
        default:
            return state;
    }
}

export const getOutputList: any = (state: State) => state.outputList;
export const getSelectedOutputs: any = (state: State) => {
    return {
        selected: state.selectedOutputs,
        selectedIds: state.selectedOutputsIds
    };
};
export const getSelectedDisplayOption: any = (state: State) => state.selectedDisplayOption;
export const getEditedOutput: any = (state: State) => state.editedOutput;
export const isOutputSaved: any = (state: State) => state.isSaved;

