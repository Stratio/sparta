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

import { BackupType } from 'app/models/backup.model';
import * as backupsActions from './../actions/backups';
import { orderBy } from '@utils';

export interface State {
    backupList: Array<BackupType>;
    selectedBackups: Array<string>;
    selectAll: boolean;
    sortOrder: boolean;
    orderBy: string;
};

const initialState: State = {
    backupList: [],
    selectedBackups: [],
    selectAll: false,
    sortOrder: true,
    orderBy: 'fileName',
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case backupsActions.LIST_BACKUP_COMPLETE: {
            return Object.assign({}, state, {
                backupList: action.payload
            });
        }
        case backupsActions.SELECT_BACKUP: {
            return Object.assign({}, state, {
                selectedBackups: [...state.selectedBackups, action.payload],
            });
        }
        case backupsActions.UNSELECT_BACKUP: {
            return Object.assign({}, state, {
                selectedBackups: state.selectedBackups.filter(((backup: any) => {
                    return backup !== action.payload;
                })),
            });
        }
        case backupsActions.DELETE_BACKUP_COMPLETE: {
            return Object.assign({}, state, {
                selectedBackups: [],
            });
        }
        case backupsActions.SELECT_ALL_BACKUPS: {
            return Object.assign({}, state, {
                selectedBackups: action.payload ? state.backupList.map(((backup: any) => {
                    return backup.fileName;
                })) : []
            });
        }
        case backupsActions.CHANGE_ORDER: {
            return Object.assign({}, state, {
                orderBy: action.payload.orderBy,
                sortOrder: action.payload.sortOrder
            });
        }
        default:
            return state;
    }
}

export const getBackupList: any = (state: State) => orderBy([...state.backupList], state.orderBy, state.sortOrder);
export const getSelectedBackups: any = (state: State) => state.selectedBackups;
