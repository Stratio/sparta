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
};

const initialState: State = {
    outputList: [],
    selectedDisplayOption: 'BLOCKS',
    editedOutput: {},
    selectedOutputs: [],
    selectedOutputsIds: []
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case outputActions.actionTypes.LIST_OUTPUT: {
            return Object.assign({}, state, {});

        }
        case outputActions.actionTypes.LIST_OUTPUT_COMPLETE: {
            return Object.assign({}, state, {
                outputList: action.payload
            });

        }
        case outputActions.actionTypes.SELECT_OUTPUT: {
            return Object.assign({}, state, {
                selectedOutputs: [...state.selectedOutputs, action.payload],
                selectedOutputsIds: [...state.selectedOutputsIds, action.payload.id]
            });
        }
        case outputActions.actionTypes.DESELECT_OUTPUT: {
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
        case outputActions.actionTypes.DELETE_OUTPUT_COMPLETE: {
            const outputId = action.payload;
            return Object.assign({}, state, {
                outputList: state.outputList.filter((output: any) => {
                    return output.id !== outputId;
                })
            });
        }
        case outputActions.actionTypes.DISPLAY_MODE: {
            return Object.assign({}, state, {
                selectedDisplayOption: state.selectedDisplayOption === 'BLOCKS' ? 'ROWS' : 'BLOCKS'
            });
        }
        case outputActions.actionTypes.EDIT_OUTPUT: {
            return Object.assign({}, state, {
                editedOutput: action.payload
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
