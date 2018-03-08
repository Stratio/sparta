/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import * as environmentActions from './../actions/environment';

export interface State {
    environmentList: Array<any>;
    environmentFilter: string;
}

const initialState: State = {
    environmentList: [],
    environmentFilter: ''
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case environmentActions.LIST_ENVIRONMENT_COMPLETE: {
            return Object.assign({}, state, {
                environmentList: action.payload
            });
        }
        default:
            return state;
    }
}

export const getEnvironmentList: any = (state: State) =>  state.environmentList;
