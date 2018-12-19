/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { StModalService } from '@stratio/egeo';
import { ActivatedRoute, Router } from '@angular/router';
import { Store, select } from '@ngrx/store';

import { Observable } from 'rxjs';

import * as fromTemplates from './../../reducers';
import * as inputActions from './../../actions/input';
import { BreadcrumbMenuService } from 'services';
import { TemplatesBaseComponent } from './templates-base.component';


@Component({
    styleUrls: ['templates-base.styles.scss'],
    templateUrl: 'templates-base.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class InputsComponent extends TemplatesBaseComponent {

    public loaded$: Observable<boolean>;

    ngOnInit() {
        super.ngOnInit();
        this.templateListSubscription = this.store.pipe(select(fromTemplates.getInputList)).subscribe((data: any) => {
            this.templateList = data;
            this._cd.markForCheck();
        });
        this.loaded$ = this.store.pipe(select(fromTemplates.isInputsLoaded));

        this.selectedDisplayOption$ = this.store.pipe(select(fromTemplates.getSelectedInputDisplayOption));

        this.selectedTemplatesSubscription = this.store.pipe(select(fromTemplates.getSelectedInputs)).subscribe((data: any) => {
            this.selectedTemplates = data;
            this._cd.markForCheck();
        });

        this.store.dispatch(new inputActions.ListInputAction());
    }

    // @Override: abstract method implementation
    duplicateTemplate(input: any) {
        this.store.dispatch(new inputActions.DuplicateInputAction(input));
    }

    // @Override: abstract method implementation
    deleteTemplates(input: any) {
        this.store.dispatch(new inputActions.DeleteInputAction(this.selectedTemplates));
    }

    // @Override: abstract method implementation
    onCheckedTemplate($event: any): void {
        if ($event.checked) {
            this.store.dispatch(new inputActions.SelectInputAction($event.value));
        } else {
            this.store.dispatch(new inputActions.DeselectInputAction($event.value));
        }
    }

    changeOrder($event: any): void {
        this.store.dispatch(new inputActions.ChangeOrderAction({
            orderBy: $event.orderBy,
            sortOrder: $event.type
        }));
    }

    // @Override: abstract method implementation
    changeDisplayOption(): void {
        this.store.dispatch(new inputActions.DisplayModeAction());
    }

    // @Override: abstract method implementation
    editTemplateAction(input: any) {
        this.store.dispatch(new inputActions.EditInputAction(input));
    }


    constructor(private translate: TranslateService, protected store: Store<fromTemplates.State>, modalService: StModalService,
        route: Router, currentActivatedRoute: ActivatedRoute, protected _cd: ChangeDetectorRef,
        breadcrumbMenuService: BreadcrumbMenuService) {
        super(store, modalService, route, currentActivatedRoute, _cd, breadcrumbMenuService);
        this.store.dispatch(new inputActions.ResetInputFormAction());
        const deleteTemplateModalTitle = 'DASHBOARD.DELETE_INPUT_TITLE';
        const deleteTemplateModalMessage = 'DASHBOARD.DELETE_INPUT_MESSAGE';
        const deleteTemplateModalMessageTitle = 'DASHBOARD.DELETE_INPUT_MESSAGE_TITLE';
        const duplicateTemplateModalTitle = 'DASHBOARD.DUPLICATE_INPUT';
        const noItemsMessage = 'TEMPLATES.INPUTS.NO_ITEMS';

        this.translate.get([deleteTemplateModalTitle, deleteTemplateModalMessage, duplicateTemplateModalTitle,
            deleteTemplateModalMessageTitle, noItemsMessage]).subscribe(
            (value: { [key: string]: string }) => {
                this.deleteTemplateModalTitle = value[deleteTemplateModalTitle];
                this.deleteTemplateModalMessage = value[deleteTemplateModalMessage];
                this.duplicateTemplateModalTitle = value[duplicateTemplateModalTitle];
                this.deleteTemplateModalMessageTitle = value[deleteTemplateModalMessageTitle];
                this.noItemsMessage = value[noItemsMessage];
            }
            );
    }

    changePage($event: any) {
        this.perPage = $event.perPage;
        this.currentPage = $event.currentPage;
        this.store.dispatch(new inputActions.ResetInputFormAction());
    }
}
