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
import * as backupsActions from 'actions/backups';

export interface State {
    backupList: Array<BackupType>;

};

const initialState: State = {
    backupList: []
};

export function reducer(state: State = initialState, action: any): State {
    switch (action.type) {
        case backupsActions.actionTypes.LIST_BACKUP_COMPLETE: {
            return Object.assign({}, state, {
                backupList: action.payload
            });

        }
        default:
            return state;
    }
}

export const getBackupList: any = (state: State) => state.backupList;

