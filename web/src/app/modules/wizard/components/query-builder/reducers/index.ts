/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';

import * as fromRoot from 'reducers';
import * as fromQueryBuilder from './queryBuilder';

export interface CrossdataState {
   queryBuilder: fromQueryBuilder.State;
}

export interface State extends fromRoot.State {
   queryBuilder: CrossdataState;
}

export const reducers = {
   queryBuilder: fromQueryBuilder.reducer
};

export const getQueryBuilderState = createFeatureSelector<CrossdataState>('queryBuilder');

export const getQueryBuilderInnerState = createSelector(
   getQueryBuilderState,
   state => state.queryBuilder
);

export const getSelectedFields = createSelector(
   getQueryBuilderInnerState,
   state => state.selectedInputSchemas
);

export const getOutputSchemaFields = createSelector(
  getQueryBuilderInnerState,
  state => state.outputSchemaFields
);

export const getRelationPathVisibility = createSelector(
  getQueryBuilderInnerState,
  state => state.showRelationPaths
);

export const getJoin = createSelector(
   getQueryBuilderInnerState,
   state => state.join
);

export const getFilter = createSelector(
  getQueryBuilderInnerState,
  state => state.filter
);

export const getInputSchemaFields = createSelector(
   getQueryBuilderInnerState,
   state => state.inputSchemaFields
 );




