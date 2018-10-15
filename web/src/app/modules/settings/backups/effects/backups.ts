/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action, Store } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Injectable } from '@angular/core';

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/from';
import { Observable, from, forkJoin } from 'rxjs';

import * as errorActions from 'actions/errors';
import * as backupsActions from './../actions/backups';
import { BackupService } from 'app/services';
import * as fromBackups from './../reducers';


@Injectable()
export class BackupsEffect {

   @Effect()
   getBackupsList$: Observable<Action> = this.actions$
      .ofType(backupsActions.LIST_BACKUP).switchMap((response: any) =>
         this.backupService.getBackupList()
            .map((inputList: any) => new backupsActions.ListBackupCompleteAction(inputList))
            .catch((error) => from([
               new backupsActions.ListBackupErrorAction(''),
               new errorActions.ServerErrorAction(error)
            ])));


   @Effect()
   generateBackup$: Observable<Action> = this.actions$
      .ofType(backupsActions.GENERATE_BACKUP).switchMap((data: any) =>
         this.backupService.generateBackup().mergeMap((data: any) => [
            new backupsActions.GenerateBackupCompleteAction(),
            new backupsActions.ListBackupAction()
         ]).catch((error) => from([
            new backupsActions.GenerateBackupErrorAction(''),
            new errorActions.ServerErrorAction(error)
         ])));

   @Effect()
   deleteBackup$: Observable<Action> = this.actions$
      .ofType(backupsActions.DELETE_BACKUP)
      .withLatestFrom(this.store.select(state => state.backups.backups))
      .switchMap(([payload, backups]: [any, any]) => {
         const joinObservables: Observable<any>[] = [];
         backups.selectedBackups.forEach((fileName: string) => {
            joinObservables.push(this.backupService.deleteBackup(fileName));
         });
         return forkJoin(joinObservables).mergeMap(results => {
            return [new backupsActions.DeleteBackupCompleteAction(), new backupsActions.ListBackupAction()];
         }).catch(function (error: any) {
            return from([
               new backupsActions.DeleteBackupErrorAction(''),
               new errorActions.ServerErrorAction(error)
            ]);
         });
      });

   @Effect()
   downloadBackup$: Observable<Action> = this.actions$
      .ofType(backupsActions.DOWNLOAD_BACKUP)
      .withLatestFrom(this.store.select(state => state.backups.backups))
      .switchMap(([payload, backups]: [any, any]) => {
         const joinObservables: Observable<any>[] = [];
         backups.selectedBackups.forEach((fileName: string) => {
            joinObservables.push(this.backupService.downloadBackup(fileName));
         });
         return forkJoin(joinObservables);
      }).mergeMap((results: any[], index: number) => {
         results.forEach((data: any) => {
            this.backupService.createBackupFile(data, 'backup');
         });
         return from([
            new backupsActions.DownloadBackupCompleteAction('')]);
      });

   @Effect()
   executeBackup$: Observable<Action> = this.actions$
      .ofType(backupsActions.EXECUTE_BACKUP)
      .map((action: backupsActions.ExecuteBackupAction) => action.payload)
      .withLatestFrom(this.store.select(state => state.backups.backups))
      .switchMap(([data, backups]: [any, any]) => {
         return this.backupService.executeBackup(backups.selectedBackups[0], data)
            .map((response) => {
               return new backupsActions.ExecuteBackupCompleteAction('');
            }).catch(function (error) {
               return from([
                  new backupsActions.ExecuteBackupErrorAction(''),
                  new errorActions.ServerErrorAction(error)
               ]);
            });
      });

   @Effect()
   deleteAllBackups$: Observable<Action> = this.actions$
      .ofType(backupsActions.DELETE_ALL_BACKUPS).switchMap((data: any) => {
         return this.backupService.deleteAllBackups().mergeMap((res: any) => {
            return [new backupsActions.DeleteAllBackupsCompleteAction(), new backupsActions.ListBackupAction()];
         }).catch(function (error) {
            return from([
               new backupsActions.DeleteAllBackupsErrorAction(''),
               new errorActions.ServerErrorAction(error)
            ]);
         });
      });

   @Effect()
   deleteMetadata$: Observable<Action> = this.actions$
      .ofType(backupsActions.DELETE_METADATA).switchMap((data: any) => {
         return this.backupService.deleteMetadata()
            .map(() => {
               return new backupsActions.DeleteMetadataCompleteAction();
            }).catch(function (error) {
               return from([
                  new backupsActions.DeleteMetadataErrorAction(''),
                  new errorActions.ServerErrorAction(error)
               ]);
            });
      });

   @Effect()
   uploadBackup$: Observable<Action> = this.actions$
      .ofType(backupsActions.UPLOAD_BACKUP).switchMap((data: any) => {
         return this.backupService.uploadBackup(data.payload)
            .mergeMap(() => {
               return [new backupsActions.UploadBackupCompleteAction(''), new backupsActions.ListBackupAction()];
            }).catch(function (error) {
               return from([
                  new backupsActions.UploadBackupErrorAction(''),
                  new errorActions.ServerErrorAction(error)
               ]);
            });
      });

   constructor(
      private actions$: Actions,
      private backupService: BackupService,
      private store: Store<fromBackups.State>
   ) { }
}
