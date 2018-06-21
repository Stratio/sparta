/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import * as wizardActions from './../actions/wizard';
import * as debugActions from './../actions/debug';

export interface State {
  isDebugging: boolean;
  lastDebugResult: any;
  showDebugConsole: boolean;
  debugConsoleSelectedTab: string;
};

const initialState: State = {
  isDebugging: false,
  lastDebugResult: null,
  showDebugConsole: false,
  debugConsoleSelectedTab: 'Exceptions',
};

export function reducer(state: State = initialState, action: any): State {
  switch (action.type) {
    case wizardActions.RESET_WIZARD: {
      return initialState;
    }
    case debugActions.INIT_DEBUG_WORKFLOW_COMPLETE: {
      return {
        ...state,
        isDebugging: true
      };
    }
    case debugActions.GET_DEBUG_RESULT_COMPLETE: {
      const debug = action.payload;
      const debugResult = {
        debugSuccessful: debug.debugSuccessful,
        steps: {},
        genericError: null
      };
      if (debug.stepErrors) {
        for (const nodeError in debug.stepErrors) {
          const step: any = {};
          step.error = debug.stepErrors[nodeError]
          debugResult.steps[nodeError] = step;
        }
      }
      if (debug.stepResults) {
        for (const nodeResult in debug.stepResults) {
          const step: any = debugResult.steps[nodeResult] || {};
          step.result = debug.stepResults[nodeResult]
          debugResult.steps[nodeResult] = step;
        }
      }

      if (debug.genericError && debug.genericError.message) {
        debugResult.genericError = debug.genericError;
      }
      return {
        ...state,
        lastDebugResult: debugResult
      };
    }
    case debugActions.CANCEL_DEBUG_POLLING: {
      return {
        ...state,
        isDebugging: false
      };
    }
    case debugActions.CHANGE_SELECTED_CONSOLE_TAB: {
      return {
        ...state,
        debugConsoleSelectedTab: action.payload
      };
    }
    case debugActions.SHOW_DEBUG_CONSOLE: {
      return {
        ...state,
        showDebugConsole: true,
        debugConsoleSelectedTab: action.payload
      };
    }
    case debugActions.HIDE_DEBUG_CONSOLE: {
      return {
        ...state,
        showDebugConsole: false
      };
    }
    default:
      return state;
  }
}
