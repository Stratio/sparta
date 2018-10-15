/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as workflowActions from '../actions/workflow-list';
import { Order } from '@stratio/egeo';

export interface State {
  sortOrder: Order;
  sortOrderVersions: Order;
}

const initialState: State = {
  sortOrder: {
      orderBy: 'name',
      type: 1
  },
  sortOrderVersions: {
      orderBy: 'version',
      type: 1
  }
};

export function reducer(state: State = initialState, action: any): State {
  switch (action.type) {
    case workflowActions.CHANGE_ORDER: {
      return {
          ...state,
          sortOrder: action.payload
      };
    }
    case workflowActions.CHANGE_VERSIONS_ORDER: {
      return {
          ...state,
          sortOrderVersions: action.payload
      };
    }
    default:
      return state;
  }
}
