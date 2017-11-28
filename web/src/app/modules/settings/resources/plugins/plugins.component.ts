import { OnDestroy } from '@angular/core/core';
import { Subscription } from 'rxjs/Rx';
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
import resourcesMenu from '@app/settings/resources/resources-menu';

import * as fromRoot from 'reducers';
import * as resourcesActions from 'actions/resources';

import { Observable } from 'rxjs/Observable';
import {
    StTableHeader, StModalButton, StModalResponse, StModalService, StModalMainTextSize,
    StModalType, StHorizontalTab
} from '@stratio/egeo';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BreadcrumbMenuService } from 'app/services';

@Component({
    selector: 'sparta-plugins',
    templateUrl: './plugins.template.html',
    styleUrls: ['./plugins.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpartaPlugins implements OnInit, OnDestroy {

    @ViewChild('pluginModal', { read: ViewContainerRef }) target: any;

    public pluginsList$: Observable<any>;
    public deletePluginModalTitle: string;
    public deletePluginModalMessage: string;
    public options: Array<StHorizontalTab> = [];
    public selectedPlugins: Array<string>;
    public breadcrumbOptions: Array<any>;
    public activeOption = 'PLUGINS';
    public fields: StTableHeader[] = [
        { id: 'check', label: '', sortable: false },
        { id: 'fileName', label: 'Name' },
        { id: 'uri', label: 'URI' }
    ];

    private selectedPluginsSubscription: Subscription;

    ngOnInit() {
        this._modalService.container = this.target;
        this.store.dispatch(new resourcesActions.ListPluginsAction());
        this.pluginsList$ = this.store.select(fromRoot.getPluginsList);

        this.selectedPluginsSubscription = this.store.select(fromRoot.getSelectedPlugins).subscribe((selectedPlugins: Array<string>) => {
            this.selectedPlugins = selectedPlugins;
        });
    }

    public uploadPlugin(event: any) {
        this.store.dispatch(new resourcesActions.UploadPluginAction(event[0]));
    }

    public downloadPlugin(uri: string) {
        window.open(uri);
    }


    public deletePluginConfirmModal(): void {
        const buttons: StModalButton[] = [
            { icon: 'icon-trash', iconLeft: true, label: 'Delete', primary: true, response: StModalResponse.YES },
            { icon: 'icon-circle-cross', iconLeft: true, label: 'Cancel', response: StModalResponse.NO }
        ];

        this._modalService.show({
            qaTag: 'delete-input',
            modalTitle: this.deletePluginModalTitle,
            buttons: buttons,
            message: this.deletePluginModalMessage,
            mainText: StModalMainTextSize.BIG
        }).subscribe((response: any) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                this.store.dispatch(new resourcesActions.DeletePluginAction());
            }
        });
    }

    onChangedOption($event: string): void {
        this.route.navigate(['settings/resources/drivers']);
    }

    changeOrder($event: any): void {
        this.store.dispatch(new resourcesActions.ChangeOrderPlugins({
            orderBy: $event.orderBy,
            sortOrder: $event.type
        }));
    }

    checkRow(isChecked: boolean, value: any) {
        this.checkValue({
            checked: isChecked,
            value: value
        });
    }

    checkValue($event: any): void {
        if ($event.checked) {
            this.store.dispatch(new resourcesActions.SelectPluginAction($event.value.fileName));
        } else {
            this.store.dispatch(new resourcesActions.UnselectPluginAction($event.value.fileName));
        }
    }


    constructor(private store: Store<fromRoot.State>, private _modalService: StModalService, private translate: TranslateService,
        private route: Router, private currentActivatedRoute: ActivatedRoute, public breadcrumbMenuService: BreadcrumbMenuService) {
        this.options = resourcesMenu;
        this.breadcrumbOptions = breadcrumbMenuService.getOptions();
        const deletePluginModalTitle = 'DASHBOARD.DELETE_PLUGIN_TITLE';
        const deletePluginModalMessage = 'DASHBOARD.DELETE_PLUGIN_MESSAGE';
        this.translate.get([deletePluginModalTitle, deletePluginModalMessage]).subscribe(
            (value: { [key: string]: string }) => {
                this.deletePluginModalTitle = value[deletePluginModalTitle];
                this.deletePluginModalMessage = value[deletePluginModalMessage];

            }
        );
    }

    ngOnDestroy(): void {
        this.selectedPluginsSubscription && this.selectedPluginsSubscription.unsubscribe();
    }

}
