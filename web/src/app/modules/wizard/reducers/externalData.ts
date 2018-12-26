/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as externalDataActions from './../actions/externalData';

export interface State {
  globalVariables: Array<any>;
  environmentVariables: Array<any>;
  customGroups: any;
  mlModels: Array<string>;
}

const initialState: State = {
  globalVariables: [],
  environmentVariables: [],
  customGroups: [],
  mlModels: []
};

export function reducer(state: State = initialState, action: any): State {
  switch (action.type) {

    case externalDataActions.GET_PARAMS_LIST_COMPLETE: {
      const groups = action.payload[0];
      return {
        ...state,
        globalVariables: action.payload[1].variables,
        environmentVariables: groups.find(group => group.name === 'Environment').parameters,
        customGroups: groups.filter(group => group.name !== 'Environment')
      };
    }
    case externalDataActions.GET_ML_MODELS_COMPLETE: {
      return {
        ...state,
        mlModels: action.payload.map(mlModel => mlModel.name)
      };
    }
    default:
      return state;
  }
}
