/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as workflowActions from '../actions/workflows';

// cross-cutting concerns because here `state` is the whole state tree
export const crossReducers = (state, action) => {
   switch (action.type) {
      case workflowActions.VALIDATE_SELECTED: {
         const selectedWorkflows = state.workflows.selectedWorkflowsIds.length ?
            state.workflows.workflowList.filter((workflow: any) => state.workflows.selectedWorkflowsIds.indexOf(workflow.id) > -1
               && (state.filters.currentFilterStatus === '' || workflow.filterStatus === state.filters.currentFilterStatus)) : [];
         return {
            ...state,
            workflows: {
               ...state.workflows,
               selectedWorkflows: selectedWorkflows,
               selectedWorkflowsIds: selectedWorkflows.map(workflow => workflow.id)
            }
         };
      };
      default:
         return state;
   }
};
