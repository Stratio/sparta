/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Order } from '@stratio/egeo';

import * as filtersActions from '../actions/filters';

export interface State {
  currentFilterStatus: string;
  searchQuery: string;
  paginationOptions: {
    currentPage: number;
    perPage: number;
  };
  workflowOrder: Order;
}

const initialState: State = {
  currentFilterStatus: '',
  searchQuery: '',
  paginationOptions: {
    currentPage: 1,
    perPage: 10
  },
  workflowOrder: {
    orderBy: name,
    type: 1
  }
};


export function reducer(state: State = initialState, action: any): State {
  switch (action.type) {
    case filtersActions.SEARCH_WORKFLOWS: {
      return  {
        ...state,
        searchQuery: action.payload,
        paginationOptions: {
          ...state.paginationOptions,
          currentPage: 1
        }
      };
    }
    case filtersActions.CHANGE_ORDER: {
      return {
        ...state,
        workflowOrder: action.payload
      };
    }
    case filtersActions.CHANGE_FILTER: {
      return {
        ...state,
        currentFilterStatus: action.payload,
        paginationOptions: {...state.paginationOptions, currentPage: 1 }
      };
    }
    case filtersActions.SET_PAGINATION_NUMBER: {
      return {
        ...state,
        paginationOptions: action.payload
      };
    }
    default:
      return state;
  }
}

export const getSearchQuery = (state: State) => state.searchQuery;
