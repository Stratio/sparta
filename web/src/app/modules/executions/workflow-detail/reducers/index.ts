/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';
import * as workflowDetailActions from '../actions/workflow-detail';
import * as fromRoot from "reducers";

export interface State extends fromRoot.State{
  workflowDetail: WorkflowDetail;
}

export interface WorkflowState {
  workflowDetail: WorkflowDetail;
}

export interface WorkflowDetail {
  execution: any;
  loading: boolean;
}

const initialState: WorkflowDetail = {
  execution: null,
  loading: false
};

export const getWorkflowState = createFeatureSelector<WorkflowState>('workflowDetail');

export default function reducer(state: WorkflowDetail = initialState, action: any): WorkflowDetail {
  switch (action.type) {

    case workflowDetailActions.GET_WORKFLOW_DETAIL: {
      return {
        ...state,
        execution: null,
        loading: false
      };
    }

    case workflowDetailActions.GET_WORKFLOW_DETAIL_COMPLETE: {
      return {
        ...state,
        execution: action,
        loading: false
      };
    }

    default:
      return state;
  }
}

export const workflowDetailReducers = {
  workflowDetail: reducer
}

export const getWorkflowDetail = createSelector(getWorkflowState, state =>  {
  return state.workflowDetail
});
export const getWorkflowDetailIsLoading = createSelector(getWorkflowState, state => {
  return state.workflowDetail
});
