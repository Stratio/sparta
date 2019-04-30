/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { createSelector } from 'reselect';
import { createFeatureSelector, ActionReducerMap, State } from '@ngrx/store';
import * as fromRoot from 'reducers';
import * as fromExecutionDetail from './execution-detail';
import * as executionDetailTypes from '../types/execution-detail';


export interface ExecutionState {
  executionDetail: executionDetailTypes.ExecutionDetail;
}

export interface State extends fromRoot.State {
  executionDetail: executionDetailTypes.ExecutionDetail;
}

export const reducers = {
  executionDetail: fromExecutionDetail.reducer
};

export const executionState = createFeatureSelector<ExecutionState>('executionDetail');

export const executionDetailState = createSelector(
  executionState,
  state => state.executionDetail
);

export const executionDetailInfoState = createSelector(
  executionDetailState,
  state => state.info
);

export const executionDetailParametersState = createSelector(
  executionDetailState,
  state => state.parameters
);

export const executionDetailStatusesState = createSelector(
  executionDetailState,
  state => state.statuses
);

export const executionDetailShowedState = createSelector(
  executionDetailState,
  state => state.showedActions
);

export const qualityRulesState = createSelector(
  executionDetailState,
  state => state.qualityRules
);

export const executionDetailfilterParametersState = createSelector(
  executionDetailState,
  state => state.filterParameters
);
