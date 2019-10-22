/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import * as userActions from 'actions/user';

export interface State {
    userName: string;
    editFromMonitoring: boolean;
    xDSparkUi: string;
    timeout: number;
    isCiCdEnabled: boolean;
}

const initialState: State = {
    userName: 'Sparta',
    editFromMonitoring: false,
    xDSparkUi: '',
    timeout: 20,
    isCiCdEnabled: false
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case userActions.GET_USER_PROFILE_COMPLETE: {
          const username = action.payload.userName;
          return {
              ...state,
              userName: username && username.length ? username : 'Sparta',
              xDSparkUi: action.payload.xDSparkUi,
              timeout: action.payload.timeout,
              isCiCdEnabled: action.payload.isCiCdEnabled
          };
        }
        case userActions.SET_EDIT_MONITORING_MODE: {
            return {
               ...state,
                editFromMonitoring: action.payload
            };
        }
        default:
            return state;
    }
}

export const getUsername: any = (state: State) => state.userName;
export const getEditMonitoringMode: any = (state: State) => state.editFromMonitoring;
export const getSparkUILink: any = (state: State) => state.xDSparkUi;
export const getIsCiCdEnabled: any = (state: State) => state.isCiCdEnabled;
export const getSpartaTimeout: any = (state: State) => state.timeout;
