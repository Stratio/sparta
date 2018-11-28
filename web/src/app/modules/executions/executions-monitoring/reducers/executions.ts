/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as executionActions from '../actions/executions';
import { formatDate } from '@utils';

import { isEqual } from 'lodash';
import { Order } from '@stratio/egeo';

const images = {
   running: 'runIcon',
   failed: 'failIcon',
   stopped: 'stopIcon',
   archived: 'archiveIcon'
};

export interface State {
   executionList: Array<any>;
   executionInfo: any;
   loading: boolean;
   filters: Array<any>;
   order: Order;
}

const initialState: State = {
   executionList: [],
   executionInfo: null,
   loading: false,
   filters: [
      { name: 'running', value: 0, image: 'runIcon'},
      { name: 'stopped', value: 0, image: 'stopIcon'},
      { name: 'failed', value: 0, image: 'failIcon'},
      { name: 'archived', value: 0, image: 'archiveIcon'}
   ],
    order: {
      orderBy: 'startDateWithStatus',
      type: 0
   }
};

export function reducer(state: State = initialState, action: any): State {

   switch (action.type) {
      case executionActions.LIST_EXECUTIONS_COMPLETE: {
         const { executionList, executionsSummary: summary } = action.payload;

         const filters = Object.keys(summary)
            .map(key => ({ name: key, value: summary[key], image: images[key] }));
         return {
            ...state,
            loading: false,
            executionList: isEqual(executionList, state.executionList) ? state.executionList : executionList,
            filters: isEqual(filters, state.filters) ? state.filters : filters
         };
      }

      default:
         return state;
   }
}


export const getExecutionInfo = (state: State) => state.executionInfo;
