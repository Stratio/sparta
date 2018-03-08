/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { StModalService } from '@stratio/egeo';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Rx';

import { TemplatesBaseComponent } from './templates-base.component';
import { BreadcrumbMenuService } from 'services';
import * as fromTemplates from './../../reducers';
import * as outputActions from './../../actions/output';

@Component({
    styleUrls: ['templates-base.styles.scss'],
    templateUrl: 'templates-base.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class OutputsComponent extends TemplatesBaseComponent {

    public loaded$: Observable<boolean>;

    ngOnInit() {
        super.ngOnInit();
        this.loaded$ = this.store.select(fromTemplates.isOutputsLoaded);

        this.templateListSubscription = this.store.select(fromTemplates.getOutputList).subscribe((data: any) => {
            this.templateList = data;
            this._cd.markForCheck();
        });
        this.selectedDisplayOption$ = this.store.select(fromTemplates.getSelectedOutputDisplayOption);
        this.selectedTemplatesSubscription = this.store.select(fromTemplates.getSelectedOutputs).subscribe((data: any) => {
            this.selectedTemplates = data;
            this._cd.markForCheck();
        });
        this.store.dispatch(new outputActions.ListOutputAction);
    }

    // abstract method implementation
    duplicateTemplate(output: any) {
        this.store.dispatch(new outputActions.DuplicateOutputAction(output));
    }

    // abstract method implementation
    deleteTemplates(input: any) {
        this.store.dispatch(new outputActions.DeleteOutputAction(this.selectedTemplates));
    }

    // abstract method implementation
    onCheckedTemplate($event: any): void {
        if ($event.checked) {
            this.store.dispatch(new outputActions.SelectOutputAction($event.value));
        } else {
            this.store.dispatch(new outputActions.DeselectOutputAction($event.value));
        }
    }

    changeOrder($event: any): void {
        this.store.dispatch(new outputActions.ChangeOrderAction({
            orderBy: $event.orderBy,
            sortOrder: $event.type
        }));
    }

    // abstract method implementation
    changeDisplayOption(): void {
        this.store.dispatch(new outputActions.DisplayModeAction());
    }

    // abstract method implementation
    editTemplateAction(output: any) {
        this.store.dispatch(new outputActions.EditOutputAction(output));
    }

    changePage($event: any) {
        this.perPage = $event.perPage;
        this.currentPage = $event.currentPage;
        this.store.dispatch(new outputActions.ResetOutputFormAction());
    }

    constructor(private translate: TranslateService, store: Store<fromTemplates.State>, modalService: StModalService,
        route: Router, currentActivatedRoute: ActivatedRoute, cd: ChangeDetectorRef,
        breadcrumbMenuService: BreadcrumbMenuService) {
        super(store, modalService, route, currentActivatedRoute, cd, breadcrumbMenuService);
        this.store.dispatch(new outputActions.ResetOutputFormAction());

        const deleteTemplateModalTitle = 'DASHBOARD.DELETE_OUTPUT_TITLE';
        const deleteTemplateModalMessage = 'DASHBOARD.DELETE_OUTPUT_MESSAGE';
        const duplicateTemplateModalTitle = 'DASHBOARD.DUPLICATE_OUTPUT';
        const deleteTemplateModalMessageTitle = 'DASHBOARD.DELETE_OUTPUT_MESSAGE_TITLE';
        const noItemsMessage = 'TEMPLATES.OUTPUTS.NO_ITEMS';
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
}
