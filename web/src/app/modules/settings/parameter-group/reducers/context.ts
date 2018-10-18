/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as alertParamsActions from './../actions/alert';

export interface State {
   loading: boolean;
   message: string;
}

const initialState: State = {
   loading: false,
   message: ''
};

export function reducer(state: State = initialState, action: any): State {
   switch (action.type) {
      case alertParamsActions.SHOW_LOADING: {
         return { ...state, loading: true };
      }

      case alertParamsActions.HIDE_LOADING: {
         return { ...state, loading: false };
      }

      case alertParamsActions.SHOW_ALERT: {
         return { ...state, message: action.message, loading: false };
      }

      case alertParamsActions.HIDE_ALERT: {
         return { ...state, message: '' };
      }

      default:
         return state;
   }
}
