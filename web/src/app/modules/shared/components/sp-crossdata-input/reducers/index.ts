/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */


import { createFeatureSelector, createSelector } from '@ngrx/store';
import { CrossdataInputState } from './crossdata';

 import * as fromRoot from 'reducers';
import { StDropDownMenuItem } from '@stratio/egeo';

 export interface State extends fromRoot.State {
  createAssetsState: CrossdataInputState;
}

 export { reducer } from './crossdata';

 export const getProjectAssetsFeatureState = createFeatureSelector<CrossdataInputState>('crossdataInput');

 export const getDatabases = createSelector(getProjectAssetsFeatureState, state => state.databases);
export const getDatabasesOptions = createSelector(getDatabases, (databases: Array<string>) =>
  databases.map((database: string): StDropDownMenuItem => ({
    label: database,
    value: database
  })));

 export const getDatabaseTables = (database: string) => createSelector(
  getProjectAssetsFeatureState,
  state => state.databasetables[database] ?
    state.databasetables[database]
      .map((db: string): StDropDownMenuItem => ({
        label: db,
        value: db
      })) : []
);
