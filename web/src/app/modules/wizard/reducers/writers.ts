/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as wizardActions from './../actions/wizard';
import { cloneDeep } from 'lodash';

export interface State {
  writers: { [nodeName: string]: any };
}

const initialState: State = {
  writers: {}
};

export function reducer(state: State = initialState, action: wizardActions.Actions): State {
  switch (action.type) {
    case wizardActions.CREATE_NODE_RELATION_COMPLETE: {
      if (!action.payload.writer) {
        return state;
      }
      const writers = cloneDeep(state.writers);
      if (!writers[action.payload.originId]) {
        writers[action.payload.originId] = {};
      }
      if (!writers[action.payload.originId][action.payload.destinationId]) {
        writers[action.payload.originId][action.payload.destinationId] = action.payload.writer;
      }
      return {
        ...state,
        writers
      };
    }
    case wizardActions.SAVE_NODE_WRITER_OPTIONS: {
      return {
        ...state,
        writers: {
          ...state.writers,
          [action.nodeId]: action.writers
        }
      };
    }
    case wizardActions.SET_WORKFLOW_WRITERS: {
      return {
        ...state,
        writers: action.writers
      };
    }
    case wizardActions.PASTE_NODES_COMPLETE: {
      return {
        ...state,
        writers: {
          ...state.writers,
          ...action.payload.writers
        }
      };
    }
    default:
      return state;
  }
}
