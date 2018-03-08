/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import * as crossdataActions from './../actions/crossdata';
import { orderBy } from '@utils';

export interface State {
    databases: Array<any>;
    tableList: Array<any>;
    selectedTables: Array<string>;
    queryResult: Array<any>;
    queryError: '';
    selectedDatabase: string;
    showTemporaryTables: boolean;
    tablesSortOrder: boolean;
    tablesOrderBy: string;
    searchTables: string;
    loadingDatabases: boolean;
    loadingTables: boolean;
    loadingQuery: boolean;
};

const initialState: State = {
    databases: [],
    tableList: [],
    selectedTables: [],
    queryResult: null,
    selectedDatabase: 'default',
    showTemporaryTables: false,
    searchTables: '',
    queryError: '',
    tablesSortOrder: true,
    tablesOrderBy: 'name',
    loadingDatabases: false,
    loadingTables: false,
    loadingQuery: false
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case crossdataActions.GET_DATABASES: {
            return Object.assign({}, state, {
                loadingDatabases: true
            });
        }
        case crossdataActions.GET_DATABASES_COMPLETE: {
            return Object.assign({}, state, {
                databases: action.payload,
                loadingDatabases: false
            });
        }
        case crossdataActions.EXECUTE_QUERY: {
            return Object.assign({}, state, {
                loadingQuery: true
            });
        }
        case crossdataActions.EXECUTE_QUERY_COMPLETE: {
            return Object.assign({}, state, {
                loadingQuery: false,
                queryResult: action.payload,
                queryError: ''
            });
        }
        case crossdataActions.EXECUTE_QUERY_ERROR: {
            return Object.assign({}, state, {
                queryError: action.payload,
                loadingQuery: false
            });
        }
        case crossdataActions.SHOW_TEMPORARY_TABLES: {
            return Object.assign({}, state, {
                showTemporaryTables: action.payload
            });
        }
        case crossdataActions.CHANGE_TABLES_ORDER: {
            return Object.assign({}, state, {
                tablesOrderBy: action.payload.orderBy,
                tablesSortOrder: action.payload.sortOrder
            });
        }
        case crossdataActions.FILTER_TABLES: {
            return Object.assign({}, state, {
                searchTables: action.payload
            });
        }
        case crossdataActions.SELECT_DATABASE: {
            return Object.assign({}, state, {
                selectedDatabase: action.payload
            });
        }
        case crossdataActions.LIST_DATABASE_TABLES: {
            return Object.assign({}, state, {
                loadingTables: true
            });
        }
        case crossdataActions.LIST_DATABASE_TABLES_COMPLETE: {
            return Object.assign({}, state, {
                loadingTables: false,
                tableList: action.payload,
                selectedTables: []
            });
        }
        case crossdataActions.SELECT_TABLE: {
            return Object.assign({}, state, {
                selectedTables: [...state.selectedTables, action.payload.name]
            });
        }
        case crossdataActions.UNSELECT_TABLE: {
            return Object.assign({}, state, {
                selectedTables: state.selectedTables.filter((table: string) => {
                    return table !== action.payload.name;
                })
            });
        }
        case crossdataActions.GET_TABLE_INFO_COMPLETE: {
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
    const tables = orderBy(Object.assign([],
        state.showTemporaryTables ? state.tableList : state.tableList.filter((table: any) => {
            return !(table.isTemporary);
        })), state.tablesOrderBy, state.tablesSortOrder);

    if (state.searchTables.length) {
        const filterFields = ['name', 'database', 'tableType', 'isTemporary'];
        return tables.filter((table: any) => {
            for (let i = 0; i < filterFields.length; i++) {
                let value = table[filterFields[i]];
                value = value ? value.toString().toUpperCase() : '';
                if (value.indexOf(state.searchTables.toUpperCase()) > -1) {
                    return true;
                }
            };
            return false;
        });
    } else {
        return tables;
    }
};
export const getSelectedTables: any = (state: State) => state.selectedTables;
export const getSelectedDatabase: any = (state: State) => state.selectedDatabase;
export const getDatabases: any = (state: State) => state.databases;
export const getQueryResult: any = (state: State) => state.queryResult;
export const getQueryError: any = (state: State) => state.queryError;
export const isLoadingDatabases: any = (state: State) => state.loadingDatabases;
export const isLoadingTables: any = (state: State) => state.loadingTables;
export const isLoadingQuery: any = (state: State) => state.loadingQuery;
