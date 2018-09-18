/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as executionActions from '../actions/executions';

 const icons = {
    running: 'icon-play',
    failed: 'icon-arrow-down',
    stopped: 'icon-pause',
    archived: 'icon-folder'
 };

export interface State {
  executionList: Array<any>;
  executionInfo: any;
  loading: boolean;
  filters: Array<any>;
}

const initialState: State = {
   executionList: [],
   executionInfo: null,
   loading: true,
   filters: []
};

export function reducer(state: State = initialState, action: any): State {

   switch (action.type) {

      case executionActions.LIST_EXECUTIONS_COMPLETE: {
         const { lastExecutions, executionsSummary: summary } = action.payload;
         const executionList = lastExecutions.map(execution => {
            const { id, statuses, localExecution: { sparkURI }, genericDataExecution: { endDate, startDate, workflow: { name, group, executionEngine }, executionContext: { paramsLists: context }  }} = execution;
            return { id, name, sparkURI, endDate, startDate, group, executionEngine, context: context.join(), status: statuses.length && statuses[0].state };
         });
         const filters = Object.keys(summary)
            .map(key => ({ name: key, value: summary[key], icon: icons[key] }));
         return { ...state, executionList, filters };
      }

      default:
         return state;
   }
}


export const getExecutionInfo = (state: State) => state.executionInfo;
