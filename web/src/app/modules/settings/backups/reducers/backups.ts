/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { BackupType } from 'app/models/backup.model';
import * as backupsActions from './../actions/backups';
import { orderBy } from '@utils';

export interface State {
    loaded: boolean;
    backupList: Array<BackupType>;
    selectedBackups: Array<string>;
    selectAll: boolean;
    sortOrder: boolean;
    orderBy: string;
};

const initialState: State = {
    loaded: false,
    backupList: [],
    selectedBackups: [],
    selectAll: false,
    sortOrder: true,
    orderBy: 'fileName',
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case backupsActions.LIST_BACKUP_COMPLETE: {
            return {
                ...state,
                backupList: action.payload,
                loaded: true
            };
        }
        case backupsActions.SELECT_BACKUP: {
            return {
                ...state,
                selectedBackups: [...state.selectedBackups, action.payload],
            };
        }
        case backupsActions.UNSELECT_BACKUP: {
            return {
                ...state,
                selectedBackups: state.selectedBackups.filter(((backup: any) => {
                    return backup !== action.payload;
                })),
            };
        }
        case backupsActions.DELETE_BACKUP_COMPLETE: {
            return {
                ...state,
                selectedBackups: []
            };
        }
        case backupsActions.SELECT_ALL_BACKUPS: {
            return {
                ...state,
                selectedBackups: action.payload ? state.backupList.map(((backup: any) => {
                    return backup.fileName;
                })) : []
            };
        }
        case backupsActions.CHANGE_ORDER: {
            return {
                ...state,
                orderBy: action.payload.orderBy,
                sortOrder: action.payload.sortOrder
            };
        }
        default:
            return state;
    }
}

export const getBackupList: any = (state: State) => orderBy([...state.backupList], state.orderBy, state.sortOrder);
export const getSelectedBackups: any = (state: State) => state.selectedBackups;
