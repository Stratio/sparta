/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as customParamsActions from './../actions/custom';

export interface State {
   customVariables: Array<any>;
   customList: Array<any>;
   contexts: Array<any>;
   listMode: boolean;
   list: any;
   allVariables: Array<any>;
   allCustomList: Array<any>;
   creationMode: boolean;
}

const initialState: State = {
   customVariables: [],
   customList: [],
   contexts: [],
   listMode: true,
   list: null,
   allVariables: [],
   allCustomList: [],
   creationMode: false
};

export function reducer(state: State = initialState, action: any): State {
   switch (action.type) {

      case customParamsActions.LIST_CUSTOM_PARAMS_COMPLETE: {
         return {
            ...state,
            customList: action.customLists,
            allCustomList: action.customLists,
            customVariables: [],
            allVariables: [],
            list: null,
            creationMode: false
         };
      }

      case customParamsActions.NAVIGAGE_TO_LIST_COMPLETE: {
         const { parameterList, contexts } = action.customParameters;
         const { id, name, parameters } = parameterList;
         const contextsVar = contexts
            .map(context => ({
               name: context.name,
               id: context.id,
               parameters: context.parameters.reduce((obj, item) => {
                  obj[item.name] = item.value;
                  return obj;
               }, {})
            }));

         const customVariables = parameters
            .map(env => ({
               ...env,
               defaultValue: env.value,
               contexts: contextsVar
                  .filter(c => c.parameters[env.name] && c.parameters[env.name] !== env.value)
                  .map(c => ({ name: c.name, value: c.parameters[env.name], id: c.id }))
            }));

         return { ...state, customVariables, allVariables: customVariables, list: { id, name }, contexts, customList: [], allCustomList: [], creationMode: false };

      }

      case customParamsActions.ADD_CUSTOM_LIST: {
         return {
            ...state,
            customList: [{ name: '', value: '', parameters: [] }, ...state.customList],
            creationMode: true
         };
      }
      case customParamsActions.ADD_CUSTOM_PARAMS: {
         return {
            ...state,
            customVariables: [{ name: '', value: '', contexts: [] }, ...state.customVariables],
            creationMode: true
         };
      }

      case customParamsActions.CHANGE_CONTEXT_OPTION: {
         if (action.context === 'default') {
            return { ...state, customVariables: state.allVariables };
         } else {
            const context = state.contexts.find(element => element.name === action.context);
            const parameters = context.parameters.map(param => {
               const paramFind = state.allVariables.find(p => p.name === param.name);
               if (paramFind) {
                  return { ...param, defaultValue: paramFind.defaultValue, contexts: paramFind.contexts };
               } else {
                  return param;
               }
            });
            return { ...state, customVariables: parameters };
         }
      }

      case customParamsActions.ADD_CONTEXT_COMPLETE: {
         return {
            ...state,
            contexts: [...state.contexts, action.context]
         };
      }

      case customParamsActions.SEARCH_CUSTOM_PARAMS: {
         if (action.text) {

            const customVariables = state.allVariables.filter(global => global.name.toLowerCase().includes(action.text.toLowerCase()));
            const customList = state.allCustomList.filter(global => global.name.toLowerCase().includes(action.text.toLowerCase()));
            return { ...state, customVariables, customList };
         } else {
            return { ...state, customVariables: state.allVariables, customList: state.allCustomList };
         }
      }

      case customParamsActions.ADD_CUSTOM_CONTEXT: {
         return {
            ...state,
            contexts: [{ name: '', parameters: state.customVariables, parent: state.list.name }, ...state.contexts],
            creationMode: true
         };
      }
      case customParamsActions.SAVE_CUSTOM_PARAMS_COMPLETE: {
         return { ...state, creationMode: false };
      }


      default:
         return state;
   }
}
