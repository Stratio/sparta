import { ɵpad } from '@angular/core';
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as executionActions from '../actions/executions';

import { Order } from '@stratio/egeo';

export interface State {
   isArchivedPage: boolean;
   executionList: Array<any>;
   archivedExecutionList: Array<any>;
   executionInfo: any;
   loading: boolean;
   loadingArchived: boolean;
   selectedExecutionsIds: Array<string>;
   statusFilter: string;
   typeFilter: string;
   timeIntervalFilter: number;
   searchQuery: string;
   pagination: {
      perPage: number;
      currentPage: number;
      total: number
   };
   order: Order;
}

const initialState: State = {
   isArchivedPage: false,
   executionList: [],
   archivedExecutionList: [],
   executionInfo: null,
   loading: true,
   loadingArchived: true,
   selectedExecutionsIds: [],
   statusFilter: '',
   typeFilter: '',
   timeIntervalFilter: 0,
   searchQuery: '',
   pagination: {
      perPage: 10,
      currentPage: 1,
      total: 0
   },
   order: {
      orderBy: 'startDateWithStatus',
      type: 0
   }
};

export function reducer(state: State = initialState, action: any): State {
   switch (action.type) {
      case executionActions.LIST_EXECUTIONS_COMPLETE: {
         const [execution, ...rest] = action.payload;
         return {
            ...state,
            executionList: action.payload,
            loading: false,
            pagination: { ...state.pagination, total: execution && execution.totalCount ? execution.totalCount : action.payload ? action.payload.length : 0 }
         };
      }
      case executionActions.LIST_EXECUTIONS_EMPTY: {
         const [execution, ...rest] = state.executionList;
         return {
            ...state,
            loading: false,
            pagination: { ...state.pagination, total: execution && execution.totalCount ? execution.totalCount : action.payload ? action.payload.length : 0 }
         };
      }
      case executionActions.LIST_ARCHIVED_EXECUTIONS_COMPLETE: {
         const [execution, ...rest] = action.payload;
         return {
            ...state,
            loadingArchived: false,
            archivedExecutionList: action.payload,
            pagination: { ...state.pagination, total: execution && execution.totalCount ? execution.totalCount : action.payload ? action.payload.length : 0 }
         };

      }
      case executionActions.SELECT_EXECUTIONS_ACTION: {
         const { execution: { id } } = action;
         return {
            ...state,
            selectedExecutionsIds: [id, ...state.selectedExecutionsIds]
         };
      }
      case executionActions.DESELECT_EXECUTIONS_ACTION: {
         const { execution: { id } } = action;
         return {
            ...state,
            selectedExecutionsIds: state.selectedExecutionsIds.filter(e => e !== id)
         };
      }
      case executionActions.SELECT_STATUS_FILTER: {
         return {
            ...state,
            statusFilter: action.status,
            selectedExecutionsIds: [],
            pagination: {
               ...state.pagination,
               currentPage: 1
            }
         };
      }
      case executionActions.ARCHIVE_EXECUTIONS_COMPLETE: {
         return {
            ...state,
            selectedExecutionsIds: []
         };
      }
      case executionActions.UNARCHIVE_EXECUTIONS_COMPLETE: {
         return {
            ...state,
            selectedExecutionsIds: []
         };
      }
      case executionActions.SELECT_TYPE_FILTER: {
         return {
            ...state,
            typeFilter: action.workflowType,
            selectedExecutionsIds: [],
            pagination: {
               ...state.pagination,
               currentPage: 1
            }
         };
      }
      case executionActions.SELECT_TIME_INTERVAL_FILTER: {
         return {
            ...state,
            timeIntervalFilter: action.time,
            selectedExecutionsIds: [],
            pagination: {
               ...state.pagination,
               currentPage: 1
            }
         };
      }
      case executionActions.SEARCH_EXECUTION: {
         return {
            ...state,
            searchQuery: action.searchQuery,
            selectedExecutionsIds: [],
            pagination: {
               ...state.pagination,
               currentPage: 1
            }
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
            pagination: {
               ...state.pagination,
               ...action.payload
            },
            selectedExecutionsIds: []
         };
      }
      case executionActions.GET_WORKFLOW_EXECUTION_INFO_COMPLETE: {
         return {
            ...state,
            executionInfo: action.payload
         };
      }
      case executionActions.CLOSE_WORKFLOW_EXECUTION_INFO: {
         return {
            ...state,
            executionInfo: null
         };
      }
      case executionActions.RESET_VALUES: {
         return {
            ...initialState,
            executionList: state.executionList
         };
      }
      case executionActions.SET_ARCHIVED_PAGE: {
         return {
            ...state,
            isArchivedPage: action.payload
         };
      }
      case executionActions.DELETE_EXECUTION_COMPLETE: {
         return {
            ...state,
            selectedExecutionsIds: [],
            executionList: state.executionList.filter(execution => execution.id !== action.executionId)
         };
      }
      default:
         return state;
   }
}
