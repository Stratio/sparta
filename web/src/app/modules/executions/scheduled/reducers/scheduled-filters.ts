/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as scheduledFilterActions from '../actions/scheduledFilterActions';

import { Order, StDropDownMenuItem } from '@stratio/egeo';
import { workflowTypesFilterOptions, timeIntervalsFilterOptions } from '../models/schedules-filters';

export interface State {
  workflowTypesFilterValue: StDropDownMenuItem;
  timeIntervalsFilterValue: StDropDownMenuItem;
}

const initialState: State = {
  workflowTypesFilterValue: workflowTypesFilterOptions[0],
  timeIntervalsFilterValue: timeIntervalsFilterOptions[0]
};

export function reducer(state: State = initialState, action: any): State {
  switch (action.type) {

    default:
      return state;
  }
}
