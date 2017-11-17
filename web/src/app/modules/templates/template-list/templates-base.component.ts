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

import { OnDestroy, OnInit, ViewContainerRef, ViewChild, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import { Router, ActivatedRoute } from '@angular/router';
import {
    StTableHeader, StModalButton, StModalResponse, StModalService,
    StModalType, StModalMainTextSize
} from '@stratio/egeo';
import { Subscription } from 'rxjs/Rx';

import * as fromRoot from 'reducers';
import { Observable } from 'rxjs/Observable';
import { BreadcrumbMenuService } from 'services';

export abstract class TemplatesBaseComponent implements OnInit, OnDestroy {

    @ViewChild('inputsModal', { read: ViewContainerRef }) target: any;

    public templateList: any = [];
    public deleteTemplateModalTitle: string;
    public deleteTemplateModalMessage: string;
    public duplicateTemplateModalTitle: string;
    public displayOptions$: Observable<Array<any>>;
    public selectedDisplayOption$: Observable<any>;
    public selectedTemplates: any = [];
    public fragmentIndex = 0;
    public breadcrumbOptions: string[] = [];
    public orderBy = 'name';
    public sortOrder = true;

    protected selectedTemplatesSubscription: Subscription;
    protected templateListSubscription: Subscription;

    public fields: StTableHeader[] = [
        { id: 'isChecked', label: '', sortable: false },
        { id: 'name', label: 'Name' },
        { id: 'description', label: 'Description' }
    ];

    ngOnInit() {
        this._modalService.container = this.target;
    }

    public deleteTemplateConfirmModal(): void {
        const buttons: StModalButton[] = [
            { icon: 'icon-trash', iconLeft: true, label: 'Delete', primary: true, response: StModalResponse.YES },
            { icon: 'icon-circle-cross', iconLeft: true, label: 'Cancel', response: StModalResponse.NO }
        ];

        this._modalService.show({
            qaTag: 'delete-template',
            modalTitle: this.deleteTemplateModalTitle,
            buttons: buttons,
            message: this.deleteTemplateModalMessage,
            mainText: StModalMainTextSize.BIG,
            modalType: StModalType.WARNING
        }).subscribe((response) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                this.deleteTemplates(this.selectedTemplates);
            }
        });
    }

    public duplicateTemplates(): void {
        this.selectedTemplates.selected.forEach((input: any) => {
            let name = '';
            let i = 0;
            do {
                name = input.name + '(' + i + ')';
                i++;
            } while (!this.isValidTemplateName(name));

            const duplicatedInput = {};
            Object.assign(duplicatedInput, input, {
                name: name
            });

            this.duplicateTemplate(duplicatedInput);
        });
    }

    public isValidTemplateName(name: string) {
        for (const template of this.templateList) {
            if (template.name === name) {
                return false;
            }
        }
        return true;
    }

    public createTemplate() {
        this.route.navigate(['create'], { relativeTo: this.currentActivatedRoute });
    }

    public editTemplate(template: any) {
        this.editTemplateAction(template);
        this.route.navigate(['edit'], { relativeTo: this.currentActivatedRoute });
    }

    checkRow(isChecked: boolean, value: any) {
        this.onCheckedTemplate({
            checked: isChecked,
            value: value
        });
    }

    constructor(protected store: Store<fromRoot.State>, private _modalService: StModalService,
        private route: Router, private currentActivatedRoute: ActivatedRoute, protected _cd: ChangeDetectorRef,
        public breadcrumbMenuService: BreadcrumbMenuService) {
        this.breadcrumbOptions = breadcrumbMenuService.getOptions();
    }

    public ngOnDestroy(): void {
        this.selectedTemplatesSubscription.unsubscribe();
        this.templateListSubscription.unsubscribe();
    }

    abstract duplicateTemplate(input: any): void;
    abstract deleteTemplates(templates: any): void;
    abstract onCheckedTemplate($event: any): void;
    abstract changeDisplayOption(): void;
    abstract editTemplateAction(input: any): void;
}
