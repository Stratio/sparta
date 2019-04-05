/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as scheduledActions from '../actions/scheduled';

import { Order } from '@stratio/egeo';
import { ScheduledExecution } from '../models/scheduled-executions';

export interface State {
  scheduledExecutionsList: Array<ScheduledExecution>;
  selectedExecutions: Array<string>;
}

const initialState: State = {
  scheduledExecutionsList: [],
  selectedExecutions: []
};

export function reducer(state: State = initialState, action: scheduledActions.ScheduledUnionActions): State {
  switch (action.type) {
    case scheduledActions.ScheduledActions.LIST_SCHEDULED_EXECUTIONS_COMPLETE: {
      return {
        ...state,
        scheduledExecutionsList: action.scheduledExecutions
      };
    }
    case scheduledActions.ScheduledActions.TOGGLE_EXECUTION_SELECTION: {
      return {
        ...state,
        selectedExecutions: state.selectedExecutions.includes(action.executionId) ? 
          state.selectedExecutions.filter(execution => execution !== action.executionId) : [...state.selectedExecutions, action.executionId]
      }
    }
    case scheduledActions.ScheduledActions.DELETE_SCHEDULED_EXECUTION_COMPLETE: {
      return {
        ...state,
        selectedExecutions: []
      };
    }
    case scheduledActions.ScheduledActions.REMOVE_SELECTION: {
      return {
        ...state,
        selectedExecutions: []
      };
    }
    case scheduledActions.ScheduledActions.TOGGLE_ALL_EXECUTIONS: {
      return {
        ...state,
        selectedExecutions: action.executionsIds
      }
    }
    default:
      return state;
  }
}
