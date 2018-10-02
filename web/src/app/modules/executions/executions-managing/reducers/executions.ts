/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as executionActions from '../actions/executions';
import { isEqual } from 'lodash';
import { Order } from '@stratio/egeo';

export interface State {
   executionList: Array<any>;
   executionInfo: any;
   loading: boolean;
   selectedExecutionsIds: Array<string>;
   statusFilter: string;
   typeFilter: string;
   timeIntervalFilter: number;
   searchQuery: string;
   pagination: {
      perPage: number;
      currentPage: number;
   };
   order: Order;
}

const initialState: State = {
   executionList: [],
   executionInfo: null,
   loading: true,
   selectedExecutionsIds: [],
   statusFilter: '',
   typeFilter: '',
   timeIntervalFilter: 0,
   searchQuery: '',
   pagination: {
      perPage: 10,
      currentPage: 1
   },
   order: {
      orderBy: 'startDateWithStatus',
      type: 0
   }
};

export function reducer(state: State = initialState, action: any): State {
   switch (action.type) {
      case executionActions.LIST_EXECUTIONS_COMPLETE: {
         const executionList = action.payload;
         return { ...state, executionList: isEqual(executionList, state.executionList) ? state.executionList : executionList };
      }

      case executionActions.SELECT_EXECUTIONS_ACTION: {
         const { execution: { id } } = action;
         return { ...state, selectedExecutionsIds: [id, ...state.selectedExecutionsIds] };
      }
      case executionActions.DESELECT_EXECUTIONS_ACTION: {

         const { execution: { id } } = action;
         return { ...state, selectedExecutionsIds: state.selectedExecutionsIds.filter(e => e !== id) };
      }
      case executionActions.SELECT_STATUS_FILTER: {
         return {
            ...state,
            statusFilter: action.status,
            selectedExecutionsIds: []
         };
      }
      case executionActions.SELECT_TYPE_FILTER: {
         return {
            ...state,
            typeFilter: action.workflowType,
            selectedExecutionsIds: []
         };
      }
      case executionActions.SELECT_TIME_INTERVAL_FILTER: {
         return {
            ...state,
            timeIntervalFilter: action.time,
            selectedExecutionsIds: []
         };
      }
      case executionActions.SEARCH_EXECUTION: {
         return {
            ...state,
            searchQuery: action.searchQuery,
            selectedExecutionsIds: []
         };
      }
      case executionActions.CHANGE_ORDER: {
         return {
            ...state,
            order: action.order
         };
      }
      case executionActions.CHANGE_PAGINATION: {
         return {
            ...state,
            pagination: action.payload,
            selectedExecutionsIds: []
         };
      }
      case executionActions.RESET_VALUES: {
         return initialState;
      }
      default:
         return state;
   }
}
