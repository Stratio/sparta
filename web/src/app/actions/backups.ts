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

import { Action } from '@ngrx/store';
import { type } from '../utils';

export const actionTypes: any = {
    LIST_BACKUP: type('[Backups] List backups'),
    LIST_BACKUP_COMPLETE: type('[Backups] List backups complete'),
    LIST_BACKUP_ERROR: type('[Backups] List backups error'),
    GENERATE_BACKUP: type('[Backups] Generate backup'),
    GENERATE_BACKUP_COMPLETE: type('[Backups] Generate backup complete'),
    GENERATE_BACKUP_ERROR: type('[Backups] Generate backup error'),
    DELETE_BACKUP: type('[Backups] delete backup'),
    DELETE_BACKUP_COMPLETE: type('[Backups] delete backup complete'),
    DELETE_BACKUP_ERROR: type('[Backups] delete backup error'),
    DOWNLOAD_BACKUP: type('[Backups] download backup'),
    DOWNLOAD_BACKUP_COMPLETE: type('[Backups] download backup complete'),
    DOWNLOAD_BACKUP_ERROR: type('[Backups] download backup error'),
    EXECUTE_BACKUP: type('[Backups] execute backup'),
    EXECUTE_BACKUP_COMPLETE: type('[Backups] execute backup complete'),
    EXECUTE_BACKUP_ERROR: type('[Backups] execute backup error'),
    DELETE_ALL_BACKUPS: type('[Backups] delete all backups'),
    DELETE_ALL_BACKUPS_COMPLETE: type('[Backups] delete all backups complete'),
    DELETE_ALL_BACKUPS_ERROR: type('[Backups] delete all backups error'),
    DELETE_METADATA: type('[Backups] delete metadata'),
    DELETE_METADATA_COMPLETE: type('[Backups] delete metadata complete'),
    DELETE_METADATA_ERROR: type('[Backups] delete metadata error'),
    UPLOAD_BACKUP: type('[Backups] upload backup'),
    UPLOAD_BACKUP_COMPLETE: type('[Backups] upload backup complete'),
    UPLOAD_BACKUP_ERROR: type('[Backups] upload backup error')
};

export class ListBackupAction implements Action {
    type: any = actionTypes.LIST_BACKUP;
    constructor() { }
}

export class ListBackupCompleteAction implements Action {
    type: any = actionTypes.LIST_BACKUP_COMPLETE;
    constructor(public payload: any) { }
}

export class ListBackupErrorAction implements Action {
    type: any = actionTypes.LIST_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class GenerateBackupAction implements Action {
    type: any = actionTypes.GENERATE_BACKUP;
    constructor() { }
}

export class GenerateBackupCompleteAction implements Action {
    type: any = actionTypes.GENERATE_BACKUP_COMPLETE;
    constructor() { }
}

export class GenerateBackupErrorAction implements Action {
    type: any = actionTypes.GENERATE_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class DeleteBackupAction implements Action {
    type: any = actionTypes.DELETE_BACKUP;
    constructor(public payload: string) { }
}

export class DeleteBackupCompleteAction implements Action {
    type: any = actionTypes.DELETE_BACKUP_COMPLETE;
    constructor(public payload: any) { }
}

export class DeleteBackupErrorAction implements Action {
    type: any = actionTypes.DELETE_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class DownloadBackupAction implements Action {
    type: any = actionTypes.DOWNLOAD_BACKUP;
    constructor(public payload: string) { }
}

export class DownloadBackupCompleteAction implements Action {
    type: any = actionTypes.DOWNLOAD_BACKUP_COMPLETE;
    constructor(public payload: any) { }
}

export class DownloadBackupErrorAction implements Action {
    type: any = actionTypes.DOWNLOAD_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class ExecuteBackupAction implements Action {
    type: any = actionTypes.EXECUTE_BACKUP;
    constructor(public payload: any) { }
}

export class ExecuteBackupCompleteAction implements Action {
    type: any = actionTypes.EXECUTE_BACKUP_COMPLETE;
    constructor(public payload: any) { }
}

export class ExecuteBackupErrorAction implements Action {
    type: any = actionTypes.EXECUTE_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class DeleteAllBackupsAction implements Action {
    type: any = actionTypes.DELETE_ALL_BACKUPS;
    constructor() { }
}

export class DeleteAllBackupsCompleteAction implements Action {
    type: any = actionTypes.DELETE_ALL_BACKUPS_COMPLETE;
    constructor() { }
}

export class DeleteAllBackupsErrorAction implements Action {
    type: any = actionTypes.DELETE_ALL_BACKUPS_ERROR;
    constructor(public payload: any) { }
}

export class DeleteMetadataAction implements Action {
    type: any = actionTypes.DELETE_METADATA;
    constructor() { }
}

export class DeleteMetadataCompleteAction implements Action {
    type: any = actionTypes.DELETE_METADATA_COMPLETE;
    constructor() { }
}

export class DeleteMetadataErrorAction implements Action {
    type: any = actionTypes.DELETE_METADATA_ERROR;
    constructor(public payload: any) { }
}

export class UploadBackupAction implements Action {
    type: any = actionTypes.UPLOAD_BACKUP;
    constructor(public payload: any) { }
}

export class UploadBackupCompleteAction implements Action {
    type: any = actionTypes.UPLOAD_BACKUP_COMPLETE;
    constructor(public payload: any) { }
}

export class UploadBackupErrorAction implements Action {
    type: any = actionTypes.UPLOAD_BACKUP_ERROR;
    constructor(public payload: any) { }
}


export type Actions =
    ListBackupAction |
    ListBackupCompleteAction |
    ListBackupErrorAction |
    GenerateBackupAction |
    GenerateBackupCompleteAction |
    GenerateBackupErrorAction |
    DeleteBackupAction | 
    DeleteBackupCompleteAction |
    DeleteBackupErrorAction |
    DownloadBackupAction | 
    DownloadBackupCompleteAction |
    DownloadBackupErrorAction |
    ExecuteBackupAction | 
    ExecuteBackupCompleteAction |
    ExecuteBackupErrorAction |
    DeleteAllBackupsAction |
    DeleteAllBackupsCompleteAction |
    DeleteAllBackupsErrorAction |
    DeleteMetadataAction |
    DeleteMetadataCompleteAction |
    DeleteMetadataErrorAction |
    UploadBackupAction |
    UploadBackupCompleteAction |
    UploadBackupErrorAction;
