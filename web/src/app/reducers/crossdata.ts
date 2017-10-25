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
    queryResult: Array<any>;
    queryError: '';
    selectedDatabase: string;
    showTemporaryTables: boolean;

};

const initialState: State = {
    databases: [],
    tableList: [],
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
                tableList: action.payload
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
export const getSelectedDatabase: any = (state: State) => state.selectedDatabase;
export const getDatabases: any = (state: State) => state.databases;
export const getQueryResult: any = (state: State) => state.queryResult;

