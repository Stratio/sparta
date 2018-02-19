///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import * as userActions from 'actions/user';

export interface State {
    userName: string;
    editFromMonitoring: boolean;
}

const initialState: State = {
    userName: '',
    editFromMonitoring: false
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case userActions.GET_USER_PROFILE_COMPLETE: {
            return Object.assign({}, state, {
                userName: action.payload.userName
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
