/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as executionParametersTypes from '../types/execution-detail';
import * as executionDetailActions from '../actions/execution-detail';

const initialState: executionParametersTypes.ExecutionDetail = {
  info: {
    name: '',
    marathonId: '',
    description: '',
    status: '',
    executionEngine: '',
    sparkURI: '',
    historyServerURI: '',
    context: [],
    launchHour: '',
    launchDate: '',
    startHour: '',
    startDate: '',
    duration: '',
    endHour: '',
    endDate: '',
    lastError: null
  },
  parameters: [{
    name: '',
    type: '',
    lastModified: '',
    completeName: '',
    selected: false
  }],
  filterParameters: '',
  statuses: [{
    name: '',
    statusInfo: '',
    startTime: ''
  }],
  showedActions: {
    showedReRun: false,
    showedStop: false,
    showedContextMenu: false,
    menuOptions: []
  },
  qualityRules: [],
  showConsole: false
};

export function reducer(state: executionParametersTypes.ExecutionDetail = initialState, action: any): executionParametersTypes.ExecutionDetail {

  switch (action.type) {
    case executionDetailActions.CREATE_EXECUTION_DETAIL:
      return {
        ...state,
        ...action.payload
      };

    case executionDetailActions.RESET_EXECUTION_DETAIL:
      return {
        ...initialState
      };

    case executionDetailActions.GET_QUALITY_RULES_COMPLETE:
      return {
        ...state,
        qualityRules: action.payload
      };

    case executionDetailActions.FILTER_PARAMETERS:
      return {
        ...state,
        filterParameters: action.filter
      };
    case executionDetailActions.SHOW_CONSOLE:
      return {
        ...state,
        showConsole: true
      };
    case executionDetailActions.HIDE_CONSOLE:
      return {
        ...state,
        showConsole: false
      };
    default:
      return state;
  }
}
