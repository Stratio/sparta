/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as scheduledFilterActions from '../actions/scheduled-filters';

import { Order, StDropDownMenuItem } from '@stratio/egeo';
import { workflowTypesFilterOptions, timeIntervalsFilterOptions } from '../models/schedules-filters';
import { ScheduledFiltersActions } from '../actions/scheduled-filters';

export interface State {
  workflowTypesFilterValue: StDropDownMenuItem;
  timeIntervalsFilterValue: StDropDownMenuItem;
  searchQuery: string;
}

const initialState: State = {
  workflowTypesFilterValue: workflowTypesFilterOptions[0],
  timeIntervalsFilterValue: timeIntervalsFilterOptions[0],
  searchQuery: ''
};

export function reducer(state: State = initialState, action: scheduledFilterActions.ScheduledFiltersUnionActions): State {
  switch (action.type) {
    case scheduledFilterActions.ScheduledFiltersActions.SEARCH_SCHEDULED_EXECUTIONS: {
      return {
        ...state,
        searchQuery: action.searchQuery
      }
    }
    case scheduledFilterActions.ScheduledFiltersActions.CHANGE_TYPE_FILTER: {
      return {
        ...state,
        workflowTypesFilterValue: action.value
      };
    }
    default:
      return state;
  }
}
