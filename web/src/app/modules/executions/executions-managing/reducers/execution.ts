/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as executionActions from '../actions/executions';

export interface State {
  execution: any;
  loading: boolean;
}

const initialState: State = {
  execution: null,
  loading: false
};

export function reducer(state: State = initialState, action: any): State {
  switch (action.type) {

    case executionActions.GET_EXECUTION: {
      return {
        ...state,
        execution: null,
        loading: false
      };
    }

    case executionActions.GET_EXECUTION_COMPLETE: {
      return {
        ...state,
        execution: action.execution,
        loading: false
      };
      return state;
    }

    case executionActions.GET_EXECUTION_ERROR: {
      return state;
    }

    default:
      return state;
  }
}
