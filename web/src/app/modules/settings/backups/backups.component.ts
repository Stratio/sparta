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

import { Component, OnInit, ViewChild, ViewContainerRef, ChangeDetectionStrategy } from '@angular/core';
import { Store } from '@ngrx/store';
import { OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs/Rx';
import { TranslateService } from '@ngx-translate/core';
import {
    StTableHeader, StModalButton, StModalResponse, StModalService
} from '@stratio/egeo';

import * as fromBackups from './reducers';
import * as backupsActions from './actions/backups';
import { BackupType } from 'app/models/backup.model';
import { ExecuteBackup } from './components/execute-backup/execute-backup.component';
import { BreadcrumbMenuService } from 'services';

@Component({
    selector: 'sparta-backups',
    templateUrl: './backups.template.html',
    styleUrls: ['./backups.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpartaBackups implements OnInit, OnDestroy {

    @ViewChild('backupsModal', { read: ViewContainerRef }) target: any;

    public backupList: Array<BackupType> = [];
    public deleteBackupModalTitle: string;
    public deleteBackupModalMessage: string;
    public executeBackupModalTitle: string;
    public deleteAllBackupsModalTitle: string;
    public deleteAllBackupsModalMessage: string;
    public deleteMetadataModalMessage: string;
    public executeFileName: string;
    public deleteMetadataModalTitle: string;
    public deleteMetadataModalMessageTitle: string;
    public breadcrumbOptions: any;
    public selectedBackups: Array<string> = [];
    public deleteAllBackupsModalMessageTitle: string;
    public deleteBackupModalMessageTitle: string;
    public fields: StTableHeader[] = [
        { id: 'fileName', label: 'Name' },
        { id: 'path', label: 'Path' },
    ];

    private selectedBackupsSubscription: Subscription;
    private backupListSubscription: Subscription;


    ngOnInit() {
        this._modalService.container = this.target;
        this.store.dispatch(new backupsActions.ListBackupAction());
        this.backupListSubscription = this.store.select(fromBackups.getBackupList).subscribe((backup: any) => {
            this.backupList = backup;
            this._cd.detectChanges();
        });
        this.selectedBackupsSubscription = this.store.select(fromBackups.getSelectedBackups).subscribe((selectedBackups: Array<string>) => {
            this.selectedBackups = selectedBackups;
            this._cd.detectChanges();
        });
    }

    selectAll($event: any) {
        this.store.dispatch(new backupsActions.SelectAllBackups($event));
    }

    public deleteBackupConfirmModal(): void {
        const buttons: StModalButton[] = [
            { label: 'Cancel', responseValue: StModalResponse.NO, classes: 'button-secondary-gray', closeOnClick: true },
            { label: 'Delete', responseValue: StModalResponse.YES, classes: 'button-critical', closeOnClick: true  }
        ];

        this._modalService.show({
            modalTitle: this.deleteBackupModalTitle,
            buttons: buttons,
            maxWidth: 500,
            message: this.deleteBackupModalMessage,
            messageTitle: this.deleteBackupModalMessageTitle
        }).subscribe((response: any) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                this.store.dispatch(new backupsActions.DeleteBackupAction());
            }
        });
    }

    public executeBackupModal(): void {
        this._modalService.show({
            modalTitle: this.executeBackupModalTitle,
            outputs: {
                onCloseExecuteModal: this.onCloseExecuteBackupModal.bind(this)
            }
        }, ExecuteBackup);
    }


    public generatebackup(): void {
        this.store.dispatch(new backupsActions.GenerateBackupAction());
    }

    public downloadBackup(): void {
        this.store.dispatch(new backupsActions.DownloadBackupAction());
    }

    public uploadBackup(event: any) {
        this.store.dispatch(new backupsActions.UploadBackupAction(event[0]));
    }

    public onCloseExecuteBackupModal(res: any) {
        if (res && res.execute) {
            this.store.dispatch(new backupsActions.ExecuteBackupAction(res.removeData));
        }
        this._modalService.close();
    }

    changeOrder($event: any): void {
        this.store.dispatch(new backupsActions.ChangeOrderAction({
            orderBy: $event.orderBy,
            sortOrder: $event.type
        }));
    }

    public deleteAllBackupsConfirmModal(): void {
        const buttons: StModalButton[] = [
            { label: 'Cancel', responseValue: StModalResponse.NO, classes: 'button-secondary-gray', closeOnClick: true },
            { label: 'Delete', responseValue: StModalResponse.YES, classes: 'button-critical', closeOnClick: true }
        ];

        this._modalService.show({
            modalTitle: this.deleteAllBackupsModalTitle,
            buttons: buttons,
            maxWidth: 500,
            messageTitle: this.deleteAllBackupsModalMessageTitle,
            message: this.deleteAllBackupsModalMessage,
        }).subscribe((response: any) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                this.store.dispatch(new backupsActions.DeleteAllBackupsAction());
            }
        });
    }

    public deleteMetadataConfirmModal(): void {
        const buttons: StModalButton[] = [
            { label: 'Cancel', responseValue: StModalResponse.NO, classes: 'button-secondary-gray', closeOnClick: true },
            { label: 'Delete', responseValue: StModalResponse.YES, classes: 'button-critical', closeOnClick: true }
        ];
        this._modalService.show({
            modalTitle: this.deleteMetadataModalTitle,
            messageTitle: this.deleteMetadataModalMessageTitle,
            buttons: buttons,
            maxWidth: 500,
            message: this.deleteMetadataModalMessage,
        }).subscribe((response: any) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                this.store.dispatch(new backupsActions.DeleteMetadataAction());
            }
        });
    }

    checkRow(isChecked: boolean, value: any) {
        this.checkValue({
            checked: isChecked,
            value: value
        });
    }

    checkValue($event: any): void {
        if ($event.checked) {
            this.store.dispatch(new backupsActions.SelectBackupAction($event.value.fileName));
        } else {
            this.store.dispatch(new backupsActions.UnselectBackupAction($event.value.fileName));
        }
    }

    constructor(private store: Store<fromBackups.State>, private _modalService: StModalService, private translate: TranslateService,
        public breadcrumbMenuService: BreadcrumbMenuService, private _cd: ChangeDetectorRef) {
        this.breadcrumbOptions = breadcrumbMenuService.getOptions();
        const deleteBackupModalTitle = 'DASHBOARD.DELETE_BACKUP_TITLE';
        const deleteBackupModalMessage = 'DASHBOARD.DELETE_BACKUP_MESSAGE';
        const executeBackupModalTitle = 'DASHBOARD.EXECUTE_BACKUP_TITLE';
        const deleteMetadataModalTitle = 'DASHBOARD.DELETE_METADATA_TITLE';
        const deleteMetadataModalMessage = 'DASHBOARD.DELETE_METADATA_MESSAGE';
        const deleteAllBackupsModalTitle = 'DASHBOARD.DELETE_ALL_BACKUPS_TITLE';
        const deleteAllBackupsModalMessage = 'DASHBOARD.DELETE_ALL_BACKUPS_MESSAGE';
        const deleteAllBackupsModalMessageTitle = 'DASHBOARD.DELETE_ALL_BACKUPS_MESSAGE_TITLE';
        const deleteBackupModalMessageTitle = 'DASHBOARD.DELETE_ALL_BACKUPS_MESSAGE_TITLE';
        const deleteMetadataModalMessageTitle = 'DASHBOARD.DELETE_METADATA_TITLE_MESSAGE';

        this.translate.get([deleteBackupModalTitle, deleteBackupModalMessage, executeBackupModalTitle,
            deleteAllBackupsModalTitle, deleteAllBackupsModalMessage, deleteMetadataModalTitle, deleteMetadataModalMessage,
            deleteAllBackupsModalMessageTitle, deleteBackupModalMessageTitle, deleteMetadataModalMessageTitle]).subscribe(
            (value: { [key: string]: string }) => {
                this.deleteBackupModalTitle = value[deleteBackupModalTitle];
                this.deleteBackupModalMessage = value[deleteBackupModalMessage];
                this.executeBackupModalTitle = value[executeBackupModalTitle];
                this.deleteMetadataModalTitle = value[deleteMetadataModalTitle];
                this.deleteMetadataModalMessage = value[deleteMetadataModalMessage];
                this.deleteAllBackupsModalTitle = value[deleteAllBackupsModalTitle];
                this.deleteAllBackupsModalMessage = value[deleteAllBackupsModalMessage];
                this.deleteAllBackupsModalMessageTitle = value[deleteAllBackupsModalMessageTitle];
                this.deleteBackupModalMessageTitle = value[deleteBackupModalMessageTitle];
                this.deleteMetadataModalMessageTitle = value[deleteMetadataModalMessageTitle];

            });
    }

    ngOnDestroy(): void {
        this.selectedBackupsSubscription && this.selectedBackupsSubscription.unsubscribe();
        this.backupListSubscription && this.backupListSubscription.unsubscribe();
    }

}
