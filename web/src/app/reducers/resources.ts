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

import * as resourcesActions from 'actions/resources';

export interface State {
    pluginsList: Array<any>;
    driversList: Array<any>;
};

const initialState: State = {
    pluginsList: [],
    driversList: []
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case resourcesActions.LIST_PLUGINS_COMPLETE: {
            return Object.assign({}, state, {
                pluginsList: action.payload
            });
        }
        case resourcesActions.LIST_DRIVERS_COMPLETE: {
            return Object.assign({}, state, {
                driversList: action.payload
            });
        }
        default:
            return state;
    }
}

export const getPluginsList: any = (state: State) => state.pluginsList;
export const getDriversList: any = (state: State) => state.driversList;

