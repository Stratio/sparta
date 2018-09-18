/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as executionActions from '../actions/executions';

export interface State {
   executionList: Array<any>;
   executionInfo: any;
   loading: boolean;
   selectedExecutionsIds: Array<string>;
}

const initialState: State = {
   executionList: [],
   executionInfo: null,
   loading: true,
   selectedExecutionsIds: []
};

export function reducer(state: State = initialState, action: any): State {
   switch (action.type) {

      case executionActions.LIST_EXECUTIONS_COMPLETE: {
         const { payload } = action;
         const executionList = payload.map(execution => {
            const { id, statuses, marathonExecution, localExecution, genericDataExecution: { endDate, startDate, workflow: { name, group, executionEngine }, executionContext: { paramsLists: context }  }} = execution;
            return { id, name, sparkURI: localExecution ? localExecution.sparkURI : marathonExecution && marathonExecution.sparkURI || '' , endDate, startDate, group, executionEngine, context: context.join(), status: statuses.length && statuses[0].state };
         });
         return { ...state, executionList, selectedExecutionsIds: [] };
      }

      case executionActions.SELECT_EXECUTIONS_ACTION: {
         const { execution: { id }} = action;
         return { ...state, selectedExecutionsIds: [id, ...state.selectedExecutionsIds] };
      }
      case executionActions.DESELECT_EXECUTIONS_ACTION: {

         const { execution: { id }} = action;
         return { ...state, selectedExecutionsIds: state.selectedExecutionsIds.filter(e => e !== id) };
      }

      default:
         return state;
   }
}
