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
import { Actions, Effect } from '@ngrx/effects';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import * as backupsActions from 'actions/backups';
import { BackupService } from 'app/services';


@Injectable()
export class BackupsEffect {


    @Effect()
    getBackupsList$: Observable<Action> = this.actions$
        .ofType(backupsActions.actionTypes.LIST_BACKUP).switchMap((response: any) => {
            return this.backupService.getBackupList()
                .map((inputList: any) => {
                    return new backupsActions.ListBackupCompleteAction(inputList);
                }).catch(function (error) {
                    return Observable.of(new backupsActions.ListBackupErrorAction(''));
                });
        });


    @Effect()
    generateBackup$: Observable<Action> = this.actions$
        .ofType(backupsActions.actionTypes.GENERATE_BACKUP).switchMap((data: any) => {
            return this.backupService.generateBackup().mergeMap((data: any) => {
                return [new backupsActions.GenerateBackupCompleteAction(), new backupsActions.ListBackupAction()];
            }).catch(function (error) {
                return Observable.of(new backupsActions.GenerateBackupErrorAction(''));
            });
        });

    @Effect()
    deleteBackup$: Observable<Action> = this.actions$
        .ofType(backupsActions.actionTypes.DELETE_BACKUP).switchMap((data: any) => {
            return this.backupService.deleteBackup(data.payload).mergeMap((res: any) => {
                return [new backupsActions.DeleteBackupCompleteAction(data.payload), new backupsActions.ListBackupAction()];
            }).catch(function (error) {
                return Observable.of(new backupsActions.DeleteBackupErrorAction(''));
            });
        });


    @Effect()
    downloadBackup$: Observable<Action> = this.actions$
        .ofType(backupsActions.actionTypes.DOWNLOAD_BACKUP).switchMap((data: any) => {
            return this.backupService.downloadBackup(data.payload)
                .map((response) => {
                    this.backupService.createBackupFile(response, data.payload);
                    return new backupsActions.DownloadBackupCompleteAction('');
                }).catch(function (error) {
                    return Observable.of(new backupsActions.DownloadBackupErrorAction(''));
                });
        });

    @Effect()
    executeBackup$: Observable<Action> = this.actions$
        .ofType(backupsActions.actionTypes.EXECUTE_BACKUP).switchMap((data: any) => {
            return this.backupService.executeBackup(data.payload.fileName, data.payload.removeData)
                .map((response) => {
                    return new backupsActions.ExecuteBackupCompleteAction('');
                }).catch(function (error) {
                    return Observable.of(new backupsActions.ExecuteBackupErrorAction(''));
                });
        });

    @Effect()
    deleteAllBackups$: Observable<Action> = this.actions$
        .ofType(backupsActions.actionTypes.DELETE_ALL_BACKUPS).switchMap((data: any) => {
            return this.backupService.deleteAllBackups().mergeMap((res: any) => {
                return [new backupsActions.DeleteAllBackupsCompleteAction(), new backupsActions.ListBackupAction()];
            }).catch(function (error) {
                return Observable.of(new backupsActions.DeleteAllBackupsErrorAction(''));
            });
        });

    @Effect()
    deleteMetadata$: Observable<Action> = this.actions$
        .ofType(backupsActions.actionTypes.DELETE_METADATA).switchMap((data: any) => {
            return this.backupService.deleteMetadata()
                .map(() => {
                    return new backupsActions.DeleteMetadataCompleteAction();
                }).catch(function (error) {
                    return Observable.of(new backupsActions.DeleteMetadataErrorAction(''));
                });
        });

    @Effect()
    uploadBackup$: Observable<Action> = this.actions$
        .ofType(backupsActions.actionTypes.UPLOAD_BACKUP).switchMap((data: any) => {
            return this.backupService.uploadBackup(data.payload)
                .mergeMap(() => {
                    return [new backupsActions.UploadBackupCompleteAction(''), new backupsActions.ListBackupAction()];
                }).catch(function (error) {
                    return Observable.of(new backupsActions.UploadBackupErrorAction(''));
                });
        });

    constructor(
        private actions$: Actions,
        private backupService: BackupService
    ) { }
}
