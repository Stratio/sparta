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

import { Component, OnInit, Output, EventEmitter, ViewChild, ViewContainerRef, ChangeDetectionStrategy } from '@angular/core';
import { Store } from '@ngrx/store';
import { BackupType } from 'app/models/backup.model';

import * as fromRoot from 'reducers';
import * as backupsActions from 'actions/backups';

import { Observable } from 'rxjs/Observable';
import { StTableHeader, StModalButton, StModalResponse, StModalService, StModalMainTextSize, 
    StModalType, StModalWidth } from '@stratio/egeo';
import { TranslateService } from '@ngx-translate/core';
import { ExecuteBackup } from './execute-backup/execute-backup.component';

@Component({
    selector: 'sparta-backups',
    templateUrl: './backups.template.html',
    styleUrls: ['./backups.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpartaBackups implements OnInit {

    @ViewChild('backupsModal', { read: ViewContainerRef }) target: any;

    public backupList$: Observable<BackupType>;
    public deleteBackupModalTitle: string;
    public deleteBackupModalMessage: string;
    public executeBackupModalTitle: string;
    public deleteAllBackupsModalTitle: string;
    public deleteAllBackupsModalMessage: string;
    public deleteMetadataModalMessage: string;
    public executeFileName: string;
    public deleteMetadataModalTitle: string;
    public fields: StTableHeader[] = [
        { id: 'fileName', label: 'Name' },
        { id: 'size', label: 'Size' },
        { id: 'actions', label: '', sortable: false }
    ];


    ngOnInit() {
        this._modalService.container = this.target;
        this.store.dispatch(new backupsActions.ListBackupAction());
        this.backupList$ = this.store.select(fromRoot.getBackupList);
    }

    public deleteBackupConfirmModal(inputId: string): void {
        const buttons: StModalButton[] = [
            { icon: 'icon-trash', iconLeft: true, label: 'Delete', primary: true, response: StModalResponse.YES },
            { icon: 'icon-circle-cross', iconLeft: true, label: 'Cancel', response: StModalResponse.NO }
        ];

        this._modalService.show({
            qaTag: 'delete-input',
            modalTitle: this.deleteBackupModalTitle,
            buttons: buttons,
            message: this.deleteBackupModalMessage,
            mainText: StModalMainTextSize.BIG,
            modalType: StModalType.WARNING
        }).subscribe((response) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                //    this.store.dispatch(new inputActions.DeleteBackupAction(inputId));
            }
        });
    }

    public executeBackupModal(fileName: string): void {
        this.executeFileName = fileName;
        this._modalService.show({
            qaTag: 'duplicate-input-modal',
            modalTitle: this.executeBackupModalTitle,
            outputs: {
                onCloseExecuteModal: this.onCloseExecuteBackupModal.bind(this)
            },
            modalWidth: StModalWidth.REGULAR,
            mainText: StModalMainTextSize.BIG,
            modalType: StModalType.NEUTRAL
        }, ExecuteBackup);
    }


    public generatebackup(): void {
        this.store.dispatch(new backupsActions.GenerateBackupAction());
    }

    public deleteBackup(fileName: string): void {
        this.store.dispatch(new backupsActions.DeleteBackupAction(fileName));
    }

    public downloadBackup(fileName: string): void {
        this.store.dispatch(new backupsActions.DownloadBackupAction(fileName));
    }

    public uploadBackup(event: any){
        this.store.dispatch( new backupsActions.UploadBackupAction(event[0]));
    }

    public onCloseExecuteBackupModal(res: any) {
        if(res && res.execute){
            this.store.dispatch(new backupsActions.ExecuteBackupAction({
                fileName: this.executeFileName,
                removeData: res.removeData
            }));
        }
        this._modalService.close();
    }

    public deleteAllBackupsConfirmModal(): void {
        const buttons: StModalButton[] = [
            { icon: 'icon-trash', iconLeft: true, label: 'Delete', primary: true, response: StModalResponse.YES },
            { icon: 'icon-circle-cross', iconLeft: true, label: 'Cancel', response: StModalResponse.NO }
        ];

        this._modalService.show({
            qaTag: 'delete-input',
            modalTitle: this.deleteAllBackupsModalTitle,
            buttons: buttons,
            message: this.deleteAllBackupsModalMessage,
            mainText: StModalMainTextSize.BIG,
            modalType: StModalType.WARNING
        }).subscribe((response) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                this.store.dispatch(new backupsActions.DeleteAllBackupsAction());
            }
        });
    }

    public deleteMetadataConfirmModal(): void {
        const buttons: StModalButton[] = [
            { icon: 'icon-trash', iconLeft: true, label: 'Delete', primary: true, response: StModalResponse.YES },
            { icon: 'icon-circle-cross', iconLeft: true, label: 'Cancel', response: StModalResponse.NO }
        ];
        this._modalService.show({
            qaTag: 'delete-input',
            modalTitle: this.deleteMetadataModalTitle,
            buttons: buttons,
            message: this.deleteMetadataModalMessage,
            mainText: StModalMainTextSize.BIG,
            modalType: StModalType.WARNING
        }).subscribe((response) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                this.store.dispatch(new backupsActions.DeleteMetadataAction());
            }
        });
    }

    constructor(private store: Store<fromRoot.State>, private _modalService: StModalService, private translate: TranslateService) {
        const deleteBackupModalTitle: string = 'DASHBOARD.DELETE_INPUT_TITLE';
        const deleteBackupModalMessage: string = 'DASHBOARD.DELETE_INPUT_MESSAGE';
        const executeBackupModalTitle: string = 'DASHBOARD.EXECUTE_BACKUP_TITLE';
        const deleteMetadataModalTitle: string = 'DASHBOARD.DELETE_METADATA_TITLE';
        const deleteMetadataModalMessage: string = 'DASHBOARD.DELETE_METADATA_MESSAGE';
        const deleteAllBackupsModalTitle: string = 'DASHBOARD.DELETE_ALL_BACKUPS_TITLE';
        const deleteAllBackupsModalMessage: string = 'DASHBOARD.DELETE_ALL_BACKUPS_MESSAGE';

        this.translate.get([deleteBackupModalTitle, deleteBackupModalMessage, executeBackupModalTitle, 
        deleteAllBackupsModalTitle, deleteAllBackupsModalMessage, deleteMetadataModalTitle, deleteMetadataModalMessage]).subscribe(
            (value: { [key: string]: string }) => {
                this.deleteBackupModalTitle = value[deleteBackupModalTitle];
                this.deleteBackupModalMessage = value[deleteBackupModalMessage];
                this.executeBackupModalTitle = value[executeBackupModalTitle];
                this.deleteMetadataModalTitle = value[deleteMetadataModalTitle];
                this.deleteMetadataModalMessage = value[deleteMetadataModalMessage];
                this.deleteAllBackupsModalTitle = value[deleteAllBackupsModalTitle];
                this.deleteAllBackupsModalMessage = value[deleteAllBackupsModalMessage];

            }
        );


    }



}
