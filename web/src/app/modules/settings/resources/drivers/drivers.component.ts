/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, ViewChild, ViewContainerRef, ChangeDetectionStrategy } from '@angular/core';
import { Store } from '@ngrx/store';
import resourcesMenu from '@app/settings/resources/resources-menu';
import * as fromRoot from 'reducers';
import * as resourcesActions from 'actions/resources';

import { Observable } from 'rxjs/Observable';
import { StTableHeader, StModalButton, StModalResponse, StModalService, StHorizontalTab } from '@stratio/egeo';
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

    public deleteDriverConfirmModal(fileName: string): void {
        const buttons: StModalButton[] = [
            { label: 'Delete', responseValue: StModalResponse.YES },
            { label: 'Cancel', responseValue: StModalResponse.NO }
        ];

        this._modalService.show({
            modalTitle: this.deleteDriverModalTitle,
            buttons: buttons,
            message: this.deleteDriverModalMessage,
        }).subscribe((response: any) => {
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
        const deleteDriverModalTitle = 'DASHBOARD.DELETE_DRIVER_TITLE';
        const deleteDriverModalMessage = 'DASHBOARD.DELETE_DRIVER_MESSAGE';

        this.options = resourcesMenu;
        this.translate.get([deleteDriverModalTitle, deleteDriverModalMessage]).subscribe(
            (value: { [key: string]: string }) => {
                this.deleteDriverModalTitle = value[deleteDriverModalTitle];
                this.deleteDriverModalMessage = value[deleteDriverModalMessage];
            }
        );
    }
}
