/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { OnDestroy, OnInit, ViewContainerRef, ViewChild, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import { Router, ActivatedRoute } from '@angular/router';
import { StTableHeader, StModalButton, StModalResponse, StModalService } from '@stratio/egeo';
import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';
import { icons } from '@app/shared/constants/icons';

import * as fromRoot from 'reducers';
import { BreadcrumbMenuService } from 'services';
import { take } from 'rxjs/operators';

export abstract class TemplatesBaseComponent implements OnInit, OnDestroy {

    @ViewChild('inputsModal', { read: ViewContainerRef }) target: any;

    public templateList: any = [];
    public deleteTemplateModalTitle: string;
    public deleteTemplateModalMessage: string;
    public deleteTemplateModalMessageTitle: string;
    public duplicateTemplateModalTitle: string;
    public displayOptions$: Observable<Array<any>>;
    public selectedDisplayOption$: Observable<any>;
    public selectedTemplates: any = [];
    public fragmentIndex = 0;
    public breadcrumbOptions: string[] = [];
    public orderBy = 'name';
    public sortOrder = true;
    public noItemsMessage = '';
    public icons = icons;

    public currentPage = 1;
    public perPage = 10;
    public perPageOptions: any = [
        { value: 10, showFrom: 0 }, { value: 20, showFrom: 0 }, { value: 30, showFrom: 0 }
    ];


    protected selectedTemplatesSubscription: Subscription;
    protected templateListSubscription: Subscription;

    public fields: StTableHeader[] = [
        { id: 'isChecked', label: '', sortable: false },
        { id: 'name', label: 'Name' },
        { id: 'classPrettyName', label: 'Type' },
        { id: 'executionEngine', label: 'Engine' },
        { id: 'description', label: 'Description' }
    ];

    ngOnInit() {
        this._modalService.container = this.target;
    }


    checkValue(event: any) {
        this.checkRow(event.checked, event.value);
    }

    public deleteTemplateConfirmModal(): void {
        const buttons: StModalButton[] = [
            { label: 'Cancel', responseValue: StModalResponse.NO, classes: 'button-secondary-gray' },
            { label: 'Delete', responseValue: StModalResponse.YES, classes: 'button-critical', closeOnClick: true }
        ];

        this._modalService.show({
            modalTitle: this.deleteTemplateModalTitle,
            buttons: buttons,
            maxWidth: 500,
            message: this.deleteTemplateModalMessage,
            messageTitle: this.deleteTemplateModalMessageTitle
        }).pipe(take(1)).subscribe((response: any) => {
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
        this.route.navigate(['edit/' + template.id], { relativeTo: this.currentActivatedRoute });
    }

    public trackByTemplateFn(index: number, item: any) {
        return item.name;
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
    abstract changePage(event: any): void;
}
