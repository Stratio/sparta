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
}

const initialState: State = {
    userName: '',
    editFromMonitoring: false,
    xDSparkUi: '',
    timeout: 20
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case userActions.GET_USER_PROFILE_COMPLETE: {
            return {
                ...state,
                userName: action.payload.userName,
                xDSparkUi: action.payload.xDSparkUi,
                timeout: action.payload.timeout
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
export const getSpartaTimeout: any = (state: State) => state.timeout;
