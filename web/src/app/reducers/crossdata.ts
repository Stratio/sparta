import { ShowTemporaryTablesAction } from '../actions/crossdata';
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

import * as crossdataActions from 'actions/crossdata';

export interface State {
    databases: Array<any>;
    tableList: Array<any>;
    selectedTables: Array<string>;
    queryResult: Array<any>;
    queryError: '';
    selectedDatabase: string;
    showTemporaryTables: boolean;

};

const initialState: State = {
    databases: [],
    tableList: [],
    selectedTables: [],
    queryResult: [],
    selectedDatabase: 'default',
    showTemporaryTables: false,
    queryError: ''
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case crossdataActions.actionTypes.GET_DATABASES_COMPLETE: {
            return Object.assign({}, state, {
                databases: action.payload
            });
        }
        case crossdataActions.actionTypes.LIST_CROSSDATA_TABLES_COMPLETE: {
            return Object.assign({}, state, {
                tableList: action.payload
            });
        }
        case crossdataActions.actionTypes.EXECUTE_QUERY_COMPLETE: {
            return Object.assign({}, state, {
                queryResult: action.payload
            });
        }
        case crossdataActions.actionTypes.EXECUTE_QUERY_ERROR: {
            return Object.assign({}, state, {
                queryError: action.payload
            });
        }
        case crossdataActions.actionTypes.SHOW_TEMPORARY_TABLES: {
            return Object.assign({}, state, {
                showTemporaryTables: action.payload
            });
        }
        case crossdataActions.actionTypes.SELECT_DATABASE: {
            return Object.assign({}, state, {
                selectedDatabase: action.payload
            });
        }
        case crossdataActions.actionTypes.LIST_DATABASE_TABLES_COMPLETE: {
            return Object.assign({}, state, {
                tableList: action.payload,
                selectedTables: []
            });
        }
        case crossdataActions.actionTypes.SELECT_TABLE: {
            console.log(action.payload);
            return Object.assign({}, state, {
                selectedTables: [...state.selectedTables, action.payload.name]
            });
        }
        case crossdataActions.actionTypes.UNSELECT_TABLE: {
            return Object.assign({}, state, {
                selectedTables: state.selectedTables.filter((table: string) => {
                    return table !== action.payload.name;
                })
            });
        }
        case crossdataActions.actionTypes.GET_TABLE_INFO_COMPLETE: {
            const info = action.payload.info;
            const fields: any = [];

            if (info && info.length) {
                for (const property in info[0]) {
                    if (info[0].hasOwnProperty(property)) {
                        fields.push({
                            id: property,
                            label: property, 
                            sortable: false
                        });
                    }
                }
            }

            return Object.assign({}, state, {
                tableList: state.tableList.map((table: any) => {
                    if (table.name === action.payload.tableName) {
                        return Object.assign({}, table, {
                            info: info,
                            infoFields: fields
                        });
                    } else {
                        return table;
                    }
                })
            });
        }
        default:
            return state;
    }
}

export const getTableList: any = (state: State) => {
    if (state.showTemporaryTables) {
        return state.tableList;
    } else {
        return state.tableList.filter((table: any) => {
            return !(table.isTemporary);
        });
    }
};
export const getSelectedTables: any = (state: State) => state.selectedTables;
export const getSelectedDatabase: any = (state: State) => state.selectedDatabase;
export const getDatabases: any = (state: State) => state.databases;
export const getQueryResult: any = (state: State) => state.queryResult;
export const getQueryError: any = (state: State) => state.queryError;

