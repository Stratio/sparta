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

import * as transformationActions from './../actions/transformation';
import { orderBy } from 'utils';

export interface State {
    transformationList: any;
    selectedDisplayOption: string;
    editedTransformation: any;
    sortOrder: boolean;
    orderBy: string;
    selectedTransformations: any;
    selectedTransformationsIds: Array<string>;
    isSaved: boolean;
}

const initialState: State = {
    transformationList: [],
    selectedDisplayOption: 'BLOCKS',
    editedTransformation: {},
    selectedTransformations: [],
    sortOrder: true,
    orderBy: 'name',
    selectedTransformationsIds: [],
    isSaved: false
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case transformationActions.LIST_TRANSFORMATION: {
            return Object.assign({}, state, {});

        }
        case transformationActions.LIST_TRANSFORMATION_COMPLETE: {
            return Object.assign({}, state, {
                transformationList: action.payload
            });

        }
        case transformationActions.SELECT_TRANSFORMATION: {
            return Object.assign({}, state, {
                selectedTransformations: [...state.selectedTransformations, action.payload],
                selectedTransformationsIds: [...state.selectedTransformationsIds, action.payload.id]
            });
        }
        case transformationActions.DESELECT_TRANSFORMATION: {
            const newSelection = state.selectedTransformations.filter((transformation: any) => {
                if (transformation.id !== action.payload.id) {
                    return transformation;
                }
            });
            return Object.assign({}, state, {
                selectedTransformations: newSelection,
                selectedTransformationsIds: newSelection.map((transformation: any) => {
                    return transformation.id;
                })
            });
        }
        case transformationActions.DELETE_TRANSFORMATION_COMPLETE: {
            return Object.assign({}, state, {
                selectedTransformations: [],
                selectedTransformationsIds: []
            });
        }
        case transformationActions.DISPLAY_MODE: {
            return Object.assign({}, state, {
                selectedDisplayOption: state.selectedDisplayOption === 'BLOCKS' ? 'ROWS' : 'BLOCKS'
            });
        }
        case transformationActions.EDIT_TRANSFORMATION: {
            return Object.assign({}, state, {
                editedTransformation: action.payload
            });
        }
        case transformationActions.UPDATE_TRANSFORMATION_COMPLETE: {
            return Object.assign({}, state, {
                isSaved: true
            });
        }
        case transformationActions.CREATE_TRANSFORMATION_COMPLETE: {
            return Object.assign({}, state, {
                isSaved: true
            });
        }
        case transformationActions.RESET_TRANSFORMATION_FORM: {
            return Object.assign({}, state, {
                isSaved: false,
                selectedTransformations: [],
                selectedTransformationsIds: [],
                sortOrder: true,
                orderBy: 'name',
            });
        }
        case transformationActions.CHANGE_ORDER: {
            return Object.assign({}, state, {
                orderBy: action.payload.orderBy,
                sortOrder: action.payload.sortOrder
            });
        }
        default:
            return state;
    }
}

export const getTransformationList: any = (state: State) => {
    return orderBy(Object.assign([], state.transformationList), state.orderBy, state.sortOrder);
};
export const getSelectedTransformations: any = (state: State) => {
    return {
        selected: state.selectedTransformations,
        selectedIds: state.selectedTransformationsIds
    };
};
export const isTransformationSaved: any = (state: State) => state.isSaved;
export const getSelectedDisplayOption: any = (state: State) => state.selectedDisplayOption;
export const getEditedTransformation: any = (state: State) => state.editedTransformation;
