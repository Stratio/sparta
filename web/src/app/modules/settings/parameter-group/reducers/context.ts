/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as globalParamsActions from './../actions/global';

export interface State {
   contexts: Array<any>;
   allContexts: Array<any>;
   list: any;
}

const initialState: State = {
   contexts: [],
   allContexts: [],
   list: null
};

export function reducer(state: State = initialState, action: any): State {
   switch (action.type) {

      /* case globalParamsActions.LIST_GLOBAL_PARAMS_COMPLETE: {
         return {
            ...state,
            allVariables: action.params,
            globalVariables: action.params
         };
      }

      case globalParamsActions.ADD_GLOBAL_PARAMS: {
         return {
            ...state,
            globalVariables: [{ name: 'newName', value: 'newValue', contexts: [] }, ...state.globalVariables]
         };
      }

      case globalParamsActions.SEARCH_GLOBAL_PARAMS: {
         if (action.text) {
            const globalVariables = state.allVariables.filter(global => global.name.toLowerCase().includes(action.text.toLowerCase()));
            return { ...state, globalVariables };
         } else {
            return { ...state, globalVariables: state.allVariables };
         }
      } */

      default:
         return state;
   }
}
