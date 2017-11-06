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

import { Action, Store } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import * as backupsActions from 'actions/backups';
import { BackupService } from 'app/services';
import * as fromRoot from 'reducers';


@Injectable()
export class BackupsEffect {


    @Effect()
    getBackupsList$: Observable<Action> = this.actions$
        .ofType(backupsActions.LIST_BACKUP).switchMap((response: any) => {
            return this.backupService.getBackupList()
                .map((inputList: any) => {
                    return new backupsActions.ListBackupCompleteAction(inputList);
                }).catch(function (error) {
                    return Observable.of(new backupsActions.ListBackupErrorAction(''));
                });
        });


    @Effect()
    generateBackup$: Observable<Action> = this.actions$
        .ofType(backupsActions.GENERATE_BACKUP).switchMap((data: any) => {
            return this.backupService.generateBackup().mergeMap((data: any) => {
                return [new backupsActions.GenerateBackupCompleteAction(), new backupsActions.ListBackupAction()];
            }).catch(function (error) {
                return Observable.of(new backupsActions.GenerateBackupErrorAction(''));
            });
        });

    @Effect()
    deleteBackup$: Observable<Action> = this.actions$
        .ofType(backupsActions.DELETE_BACKUP)
        .withLatestFrom(this.store.select(state => state.backups))
        .switchMap(([payload, backups]: [any, any]) => {
            const joinObservables: Observable<any>[] = [];
            backups.selectedBackups.forEach((fileName: string) => {
                joinObservables.push(this.backupService.deleteBackup(fileName));
            });
            return Observable.forkJoin(joinObservables).mergeMap(results => {
                return [new backupsActions.DeleteBackupCompleteAction(), new backupsActions.ListBackupAction()];
            }).catch(function (error: any) {
                return Observable.of(new backupsActions.DeleteBackupErrorAction(''));
            });
        });

    @Effect()
    downloadBackup$: Observable<Action> = this.actions$
        .ofType(backupsActions.DOWNLOAD_BACKUP)
        .withLatestFrom(this.store.select(state => state.backups))
        .switchMap(([payload, backups]: [any, any]) => {
            const joinObservables: Observable<any>[] = [];
            backups.selectedBackups.forEach((fileName: string) => {
                joinObservables.push(this.backupService.downloadBackup(fileName));
            });
            return Observable.forkJoin(joinObservables);
        }).mergeMap((results: any[], index: number) => {
            results.forEach((data: any) => {
                 this.backupService.createBackupFile(data, 'backup');
            });
            return Observable.from([new backupsActions.DownloadBackupCompleteAction('')]);
        });

    @Effect()
    executeBackup$: Observable<Action> = this.actions$
        .ofType(backupsActions.EXECUTE_BACKUP)
        .map((action: backupsActions.ExecuteBackupAction) => action.payload)
        .withLatestFrom(this.store.select(state => state.backups))
        .switchMap(([data, backups]: [any, any]) => {
            return this.backupService.executeBackup(backups.selectedBackups[0], data.payload)
                .map((response) => {
                    return new backupsActions.ExecuteBackupCompleteAction('');
                }).catch(function (error) {
                    return Observable.of(new backupsActions.ExecuteBackupErrorAction(''));
                });
        });

    @Effect()
    deleteAllBackups$: Observable<Action> = this.actions$
        .ofType(backupsActions.DELETE_ALL_BACKUPS).switchMap((data: any) => {
            return this.backupService.deleteAllBackups().mergeMap((res: any) => {
                return [new backupsActions.DeleteAllBackupsCompleteAction(), new backupsActions.ListBackupAction()];
            }).catch(function (error) {
                return Observable.of(new backupsActions.DeleteAllBackupsErrorAction(''));
            });
        });

    @Effect()
    deleteMetadata$: Observable<Action> = this.actions$
        .ofType(backupsActions.DELETE_METADATA).switchMap((data: any) => {
            return this.backupService.deleteMetadata()
                .map(() => {
                    return new backupsActions.DeleteMetadataCompleteAction();
                }).catch(function (error) {
                    return Observable.of(new backupsActions.DeleteMetadataErrorAction(''));
                });
        });

    @Effect()
    uploadBackup$: Observable<Action> = this.actions$
        .ofType(backupsActions.UPLOAD_BACKUP).switchMap((data: any) => {
            return this.backupService.uploadBackup(data.payload)
                .mergeMap(() => {
                    return [new backupsActions.UploadBackupCompleteAction(''), new backupsActions.ListBackupAction()];
                }).catch(function (error) {
                    return Observable.of(new backupsActions.UploadBackupErrorAction(''));
                });
        });

    constructor(
        private actions$: Actions,
        private backupService: BackupService,
        private store: Store<fromRoot.State>
    ) { }
}
