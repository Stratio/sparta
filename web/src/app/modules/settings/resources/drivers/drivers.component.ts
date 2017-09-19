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
import resourcesMenu from '@app/settings/resources/resources-menu';
import * as fromRoot from 'reducers';
import * as resourcesActions from 'actions/resources';

import { Observable } from 'rxjs/Observable';
import { StTableHeader, StModalButton, StModalResponse, StModalService, StModalMainTextSize, 
    StModalType, StHorizontalTab } from '@stratio/egeo';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'sparta-drivers',
    templateUrl: './drivers.template.html',
    styleUrls: ['./drivers.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpartaDrivers implements OnInit {
    @ViewChild('driverModal', { read: ViewContainerRef }) target: any;

    public driversList$: Observable<any>;
    public deleteDriverModalTitle: string;
    public deleteDriverModalMessage: string;
    public fields: StTableHeader[] = [
        { id: 'fileName', label: 'Name' },
        { id: 'uri', label: 'URI' },
        { id: 'size', label: 'Size' },
        { id: 'actions', label: '', sortable: false }
    ];

    public options: Array<StHorizontalTab> = [];

    ngOnInit() {
        this._modalService.container = this.target;
        this.store.dispatch(new resourcesActions.ListDriversAction());
        this.driversList$ = this.store.select(fromRoot.getDriversList);
    }

    public uploadDriver(event: any) {
        this.store.dispatch(new resourcesActions.UploadDriverAction(event[0]));
    }

    public downloadDriver(uri: string) {
        window.open(uri);
    }

    public deleteDriverConfirmModal(fileName: string): void {
        const buttons: StModalButton[] = [
            { icon: 'icon-trash', iconLeft: true, label: 'Delete', primary: true, response: StModalResponse.YES },
            { icon: 'icon-circle-cross', iconLeft: true, label: 'Cancel', response: StModalResponse.NO }
        ];

        this._modalService.show({
            qaTag: 'delete-input',
            modalTitle: this.deleteDriverModalTitle,
            buttons: buttons,
            message: this.deleteDriverModalMessage,
            mainText: StModalMainTextSize.BIG,
            modalType: StModalType.WARNING
        }).subscribe((response) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                this.store.dispatch(new resourcesActions.DeleteDriverAction(fileName));
            }
        });
    }

    onChangedOption($event: string): void {
        this.route.navigate(['settings/resources/plugins']);
    }

    constructor(private store: Store<fromRoot.State>, private _modalService: StModalService, private translate: TranslateService,
        private route: Router, private currentActivatedRoute: ActivatedRoute) {
        const deleteDriverModalTitle: string = 'DASHBOARD.DELETE_DRIVER_TITLE';
        const deleteDriverModalMessage: string = 'DASHBOARD.DELETE_DRIVER_MESSAGE';

        this.options = resourcesMenu;
        this.translate.get([deleteDriverModalTitle, deleteDriverModalMessage]).subscribe(
            (value: { [key: string]: string }) => {
                this.deleteDriverModalTitle = value[deleteDriverModalTitle];
                this.deleteDriverModalMessage = value[deleteDriverModalMessage];
            }
        );
    }
}
