/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as externalDataActions from './../actions/externalData';

export interface State {
  environmentVariables: Array<any>;
};

const initialState: State = {
  environmentVariables: [],
};

export function reducer(state: State = initialState, action: any): State {
  switch (action.type) {

    case externalDataActions.GET_ENVIRONMENT_LIST_COMPLETE: {
      console.log(action.payload)
      return {
        ...state,
        environmentVariables: action.payload
      };
    }
    default:
      return state;
  }
}
