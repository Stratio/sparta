/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { createSelector } from '@ngrx/store';
import { getWritersState, getEditionConfig, getNodesMap, getEdges } from '../reducers';


 export const getWritersMap = createSelector(getWritersState, state => state.writers);

 export const getEditedNodeWriters = createSelector(
  getEditionConfig,
  getWritersMap,
  getNodesMap,
  getEdges,
  (editionConfig, writers, nodesMap, edges) => {
    if (editionConfig.isEdition && editionConfig.editionType && editionConfig.editionType.data) {
      const node = editionConfig.editionType.data;
      const nodeWriters = writers[editionConfig.editionType.data.id];
      if (!nodeWriters) {
        return null;
      }
      const outputs: Array<string> = edges.reduce((acc, edge) => {
        if (edge.origin === node.name) {
          acc.push(nodesMap[edge.destination].id);
        }
        return acc;
      }, []);
      const filteredWriters = Object.keys(nodeWriters)
        .filter(key => outputs.includes(key))
        .reduce((obj, key) => {
          obj[key] = nodeWriters[key];
          return obj;
        }, {});
      return filteredWriters && Object.keys(filteredWriters).length ? filteredWriters : null;
    }
    return null;
  }
);
