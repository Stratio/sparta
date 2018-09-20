/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as environmentParamsActions from './../actions/environment';

export interface State {
   list: any;
   environmentVariables: Array<any>;
   contexts: Array<any>;
   allVariables: Array<any>;
   configContexts: Array<any>;
   creationMode: boolean;
}

const initialState: State = {
   list: null,
   environmentVariables: [],
   contexts: [],
   allVariables: [],
   configContexts: [],
   creationMode: false
};

export function reducer(state: State = initialState, action: any): State {
   switch (action.type) {

      case environmentParamsActions.LIST_ENVIRONMENT_PARAMS_COMPLETE: {
         const { parameterList, contexts } = action.params;
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

         const environmentVariables = parameters
            .map(env => ({
               ...env,
               defaultValue: env.value,
               contexts: contextsVar
                  .filter(c => c.parameters[env.name] && c.parameters[env.name] !== env.value)
                  .map(c => ({ name: c.name, value: c.parameters[env.name], id: c.id }))
            }));

         return { ...state, environmentVariables, allVariables: environmentVariables, list: { id, name }, contexts, creationMode: false };
      }

      case environmentParamsActions.ADD_CONTEXT_COMPLETE: {
         return {
            ...state,
            contexts: [...state.contexts, action.context]
         };
      }

      case environmentParamsActions.ADD_ENVIRONMENT_PARAMS: {
         return {
            ...state,
            environmentVariables: [{ name: '', value: '', contexts: [] }, ...state.environmentVariables],
            creationMode: true
         };
      }

      case environmentParamsActions.CHANGE_CONTEXT_OPTION: {
         if (action.context === 'default') {
            return { ...state, environmentVariables: state.allVariables,  creationMode: false };
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
            return { ...state, environmentVariables: parameters,  creationMode: false };
         }
      }

      case environmentParamsActions.SEARCH_ENVIRONMENT_PARAMS: {
         return { ...state, environmentVariables: action.text ?
            state.allVariables.filter(global => global.name.toLowerCase().includes(action.text.toLowerCase())) :
            state.allVariables
          };
      }

      case environmentParamsActions.ADD_ENVIRONMENT_CONTEXT: {
         return {
            ...state,
            contexts: [{ name: '', parameters: state.environmentVariables, parent: state.list.name }, ...state.contexts],
            creationMode: true
         };
      }

      default:
         return state;
   }
}
