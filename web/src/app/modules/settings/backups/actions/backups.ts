/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';

export const LIST_BACKUP = '[Backups] List backups';
export const LIST_BACKUP_COMPLETE = '[Backups] List backups complete';
export const LIST_BACKUP_ERROR = '[Backups] List backups error';
export const GENERATE_BACKUP = '[Backups] Generate backup';
export const GENERATE_BACKUP_COMPLETE = '[Backups] Generate backup complete';
export const GENERATE_BACKUP_ERROR = '[Backups] Generate backup error';
export const DELETE_BACKUP = '[Backups] delete backup';
export const SELECT_ALL_BACKUPS = '[Backups] select all backups';
export const SELECT_BACKUP = '[Backups] select backup';
export const UNSELECT_BACKUP = '[Backups] unselect backup';
export const DELETE_BACKUP_COMPLETE = '[Backups] delete backup complete';
export const DELETE_BACKUP_ERROR = '[Backups] delete backup error';
export const DOWNLOAD_BACKUP = '[Backups] download backup';
export const DOWNLOAD_BACKUP_COMPLETE = '[Backups] download backup complete';
export const DOWNLOAD_BACKUP_ERROR = '[Backups] download backup error';
export const EXECUTE_BACKUP = '[Backups] execute backup';
export const EXECUTE_BACKUP_COMPLETE = '[Backups] execute backup complete';
export const EXECUTE_BACKUP_ERROR = '[Backups] execute backup error';
export const DELETE_ALL_BACKUPS = '[Backups] delete all backups';
export const DELETE_ALL_BACKUPS_COMPLETE = '[Backups] delete all backups complete';
export const DELETE_ALL_BACKUPS_ERROR = '[Backups] delete all backups error';
export const DELETE_METADATA = '[Backups] delete metadata';
export const DELETE_METADATA_COMPLETE = '[Backups] delete metadata complete';
export const DELETE_METADATA_ERROR = '[Backups] delete metadata error';
export const UPLOAD_BACKUP = '[Backups] upload backup';
export const UPLOAD_BACKUP_COMPLETE = '[Backups] upload backup complete';
export const UPLOAD_BACKUP_ERROR = '[Backups] upload backup error';
export const CHANGE_ORDER = '[Backups] Change order';

export class ListBackupAction implements Action {
    readonly type = LIST_BACKUP;
    constructor() { }
}

export class ListBackupCompleteAction implements Action {
    readonly type = LIST_BACKUP_COMPLETE;
    constructor(public payload: any) { }
}

export class SelectAllBackups implements Action {
    readonly type = SELECT_ALL_BACKUPS;
    constructor(public payload: any) { }
}

export class SelectBackupAction implements Action {
    readonly type = SELECT_BACKUP;
    constructor(public payload: any) { }
}

export class UnselectBackupAction implements Action {
    readonly type = UNSELECT_BACKUP;
    constructor(public payload: any) { }
}

export class ListBackupErrorAction implements Action {
    readonly type = LIST_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class GenerateBackupAction implements Action {
    readonly type = GENERATE_BACKUP;
    constructor() { }
}

export class GenerateBackupCompleteAction implements Action {
    readonly type = GENERATE_BACKUP_COMPLETE;
    constructor() { }
}

export class GenerateBackupErrorAction implements Action {
    readonly type = GENERATE_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class DeleteBackupAction implements Action {
    readonly type = DELETE_BACKUP;
}

export class DeleteBackupCompleteAction implements Action {
    readonly type = DELETE_BACKUP_COMPLETE;
}

export class DeleteBackupErrorAction implements Action {
    readonly type = DELETE_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class DownloadBackupAction implements Action {
    readonly type = DOWNLOAD_BACKUP;
}

export class DownloadBackupCompleteAction implements Action {
    readonly type = DOWNLOAD_BACKUP_COMPLETE;
    constructor(public payload: any) { }
}

export class DownloadBackupErrorAction implements Action {
    readonly type = DOWNLOAD_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class ExecuteBackupAction implements Action {
    readonly type = EXECUTE_BACKUP;
    constructor(public payload: any) { }
}

export class ExecuteBackupCompleteAction implements Action {
    readonly type = EXECUTE_BACKUP_COMPLETE;
    constructor(public payload: any) { }
}

export class ExecuteBackupErrorAction implements Action {
    readonly type = EXECUTE_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class DeleteAllBackupsAction implements Action {
    readonly type = DELETE_ALL_BACKUPS;
    constructor() { }
}

export class DeleteAllBackupsCompleteAction implements Action {
    readonly type = DELETE_ALL_BACKUPS_COMPLETE;
    constructor() { }
}

export class DeleteAllBackupsErrorAction implements Action {
    readonly type = DELETE_ALL_BACKUPS_ERROR;
    constructor(public payload: any) { }
}

export class DeleteMetadataAction implements Action {
    readonly type = DELETE_METADATA;
    constructor() { }
}

export class DeleteMetadataCompleteAction implements Action {
    readonly type = DELETE_METADATA_COMPLETE;
    constructor() { }
}

export class DeleteMetadataErrorAction implements Action {
    readonly type = DELETE_METADATA_ERROR;
    constructor(public payload: any) { }
}

export class UploadBackupAction implements Action {
    readonly type = UPLOAD_BACKUP;
    constructor(public payload: any) { }
}

export class UploadBackupCompleteAction implements Action {
    readonly type = UPLOAD_BACKUP_COMPLETE;
    constructor(public payload: any) { }
}

export class UploadBackupErrorAction implements Action {
    readonly type = UPLOAD_BACKUP_ERROR;
    constructor(public payload: any) { }
}

export class ChangeOrderAction implements Action {
    readonly type = CHANGE_ORDER;
    constructor(public payload: any) { }
}


export type Actions =
    ListBackupAction |
    ListBackupCompleteAction |
    ListBackupErrorAction |
    SelectAllBackups |
    SelectBackupAction |
    UnselectBackupAction |
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
    UploadBackupErrorAction |
    ChangeOrderAction;
