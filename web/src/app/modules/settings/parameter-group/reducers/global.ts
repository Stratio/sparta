/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as globalParamsActions from './../actions/global';

export interface State {
   globalVariables: Array<any>;
   allVariables: Array<any>;
   creationMode: boolean;
}

const initialState: State = {
   globalVariables: [],
   allVariables: [],
   creationMode : false
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {

        case globalParamsActions.LIST_GLOBAL_PARAMS_COMPLETE: {
            return {
                ...state,
                allVariables: action.params,
                globalVariables: action.params,
                creationMode: false
            };
        }

        case globalParamsActions.ADD_GLOBAL_PARAMS: {
            return {
                ...state,
                globalVariables: [{ name: '', value: '', contexts: [] }, ...state.globalVariables],
                creationMode: true
            };
        }

        case globalParamsActions.SEARCH_GLOBAL_PARAMS: {
            if (action.text) {
                const globalVariables = state.allVariables.filter(global => global.name.toLowerCase().includes(action.text.toLowerCase()));
                return { ...state, globalVariables };
            } else {
                return { ...state, globalVariables: state.allVariables };
            }
        }
        case globalParamsActions.DELETE_NEW_GLOBAL_PARAMS: {
            const globalVariables = state.globalVariables.slice(1);
            return { ...state, globalVariables, allVariables: globalVariables, creationMode: false };
        }

        default:
            return state;
    }
}
