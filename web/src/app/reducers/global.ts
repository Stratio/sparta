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
}

const initialState: State = {
    userName: '',
    editFromMonitoring: false,
    xDSparkUi: ''
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case userActions.GET_USER_PROFILE_COMPLETE: {
            return Object.assign({}, state, {
                userName: action.payload.userName,
                xDSparkUi: action.payload.xDSparkUi
            });
        }
        case userActions.SET_EDIT_MONITORING_MODE: {
            return Object.assign({}, state, {
                editFromMonitoring: action.payload
            });
        }
        default:
            return state;
    }
}

export const getUsername: any = (state: State) => state.userName;
export const getEditMonitoringMode: any = (state: State) => state.editFromMonitoring;
export const getSparkUILink: any = (state: State) => state.xDSparkUi;
