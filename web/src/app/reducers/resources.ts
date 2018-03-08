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
import { orderBy } from '@utils';

export interface State {
    loaded: boolean;
    pluginsList: Array<any>;
    driversList: Array<any>;
    selectedPlugins: Array<string>;
    selectedDrivers: Array<string>;
    pluginsSortOrder: boolean;
    pluginsOrderBy: string;
    driversSortOrder: boolean;
    driversOrderBy: string;
};

const initialState: State = {
    loaded: false,
    pluginsList: [],
    driversList: [],
    selectedPlugins: [],
    selectedDrivers: [],
    driversSortOrder: true,
    driversOrderBy: 'fileName',
    pluginsSortOrder: true,
    pluginsOrderBy: 'fileName',
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case resourcesActions.LIST_PLUGINS_COMPLETE: {
            return Object.assign({}, state, {
                loaded: true,
                pluginsList: action.payload
            });
        }
        case resourcesActions.LIST_DRIVERS_COMPLETE: {
            return Object.assign({}, state, {
                driversList: action.payload
            });
        }
        case resourcesActions.SELECT_PLUGIN: {
            return Object.assign({}, state, {
                selectedPlugins: [...state.selectedPlugins, action.payload]
            });
        }
        case resourcesActions.UNSELECT_PLUGIN: {
            return Object.assign({}, state, {
                selectedPlugins: state.selectedPlugins.filter(((plugin: any) => {
                    return plugin !== action.payload;
                }))
            });
        }
        case resourcesActions.SELECT_DRIVER: {
            return Object.assign({}, state, {
                selectedDrivers: [...state.selectedDrivers, action.payload]
            });
        }
        case resourcesActions.UNSELECT_DRIVER: {
            return Object.assign({}, state, {
                selectedDrivers: state.selectedDrivers.filter(((driver: any) => {
                    return driver !== action.payload;
                }))
            });
        }
        case resourcesActions.DELETE_DRIVER_COMPLETE: {
            return Object.assign({}, state, {
                selectedDrivers: []
            });
        }
        case resourcesActions.DELETE_PLUGIN_COMPLETE: {
            return Object.assign({}, state, {
                selectedPlugins: []
            });
        }
        case resourcesActions.CHANGE_ORDER_PLUGINS: {
            return Object.assign({}, state, {
                pluginsOrderBy: action.payload.orderBy,
                pluginsSortOrder: action.payload.sortOrder
            });
        }
        case resourcesActions.CHANGE_ORDER_DRIVERS: {
            return Object.assign({}, state, {
                driversOrderBy: action.payload.orderBy,
                driversSortOrder: action.payload.sortOrder
            });
        }
        case resourcesActions.SELECT_ALL_PLUGINS: {
            return Object.assign({}, state, {
                selectedPlugins: action.payload ? state.pluginsList.map((plugin) => plugin.fileName) : []
            });
        }
        default:
            return state;
    }
}

export const getPluginsList: any = (state: State) =>
    orderBy(Object.assign([], state.pluginsList), state.pluginsOrderBy, state.pluginsSortOrder);
export const getDriversList: any = (state: State) => state.driversList;
export const getSelectedDrivers: any = (state: State) => state.selectedDrivers;
export const getSelectedPlugins: any = (state: State) => state.selectedPlugins;
export const isLoaded: any = (state: State) => state.loaded;

