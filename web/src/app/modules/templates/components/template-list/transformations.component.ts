/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TemplatesBaseComponent } from './templates-base.component';
import { TranslateService } from '@ngx-translate/core';
import { StModalService } from '@stratio/egeo';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';

import { BreadcrumbMenuService } from 'services';
import * as fromTemplates from './../../reducers';
import * as transformationActions from './../../actions/transformation';

@Component({
    styleUrls: ['templates-base.styles.scss'],
    templateUrl: 'templates-base.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class TransformationsComponent extends TemplatesBaseComponent {

    public loaded$: Observable<boolean>;

    ngOnInit() {
        super.ngOnInit();
        this.loaded$ = this.store.select(fromTemplates.isTransformationsLoaded);

        this.templateListSubscription = this.store.select(fromTemplates.getTransformationList).subscribe((data: any) => {
            this.templateList = data;
            this._cd.detectChanges();
        });

        this.selectedDisplayOption$ = this.store.select(fromTemplates.getSelectedTransformationDisplayOption);

        this.selectedTemplatesSubscription = this.store.select(fromTemplates.getSelectedTransformations).subscribe((data: any) => {
            this.selectedTemplates = data;
            this._cd.detectChanges();
        });

        this.store.dispatch(new transformationActions.ListTransformationAction());
    }

    // @Override: abstract method implementation
    duplicateTemplate(transformation: any) {
        this.store.dispatch(new transformationActions.DuplicateTransformationAction(transformation));
    }

    // @Override: abstract method implementation
    deleteTemplates(transformation: any) {
        this.store.dispatch(new transformationActions.DeleteTransformationAction(this.selectedTemplates));
    }

    // @Override: abstract method implementation
    onCheckedTemplate($event: any): void {
        if ($event.checked) {
            this.store.dispatch(new transformationActions.SelectTransformationAction($event.value));
        } else {
            this.store.dispatch(new transformationActions.DeselectTransformationAction($event.value));
        }
    }

    changeOrder($event: any): void {
        this.store.dispatch(new transformationActions.ChangeOrderAction({
            orderBy: $event.orderBy,
            sortOrder: $event.type
        }));
    }

    // @Override: abstract method implementation
    changeDisplayOption(): void {
        this.store.dispatch(new transformationActions.DisplayModeAction());
    }

    // @Override: abstract method implementation
    editTemplateAction(transformation: any) {
        this.store.dispatch(new transformationActions.EditTransformationAction(transformation));
    }

    // @Override: abstract method implementation
    changePage($event: any) {
        this.perPage = $event.perPage;
        this.currentPage = $event.currentPage;
        this.store.dispatch(new transformationActions.ResetTransformationFormAction());
    }

    constructor(private translate: TranslateService, protected store: Store<fromTemplates.State>, modalService: StModalService,
        route: Router, currentActivatedRoute: ActivatedRoute, protected _cd: ChangeDetectorRef,
        breadcrumbMenuService: BreadcrumbMenuService) {
        super(store, modalService, route, currentActivatedRoute, _cd, breadcrumbMenuService);
        this.store.dispatch(new transformationActions.ResetTransformationFormAction());
        const deleteTemplateModalTitle = 'DASHBOARD.DELETE_TRANSFORMATION_TITLE';
        const deleteTemplateModalMessage = 'DASHBOARD.DELETE_TRANSFORMATION_MESSAGE';
        const duplicateTemplateModalTitle = 'DASHBOARD.DUPLICATE_TRANSFORMATION';
        const deleteTemplateModalMessageTitle = 'DASHBOARD.DELETE_TRANSFORMATION_MESSAGE_TITLE';
        const noItemsMessage = 'TEMPLATES.TRANSFORMATIONS.NO_ITEMS';
        this.translate.get([deleteTemplateModalTitle, deleteTemplateModalMessage, duplicateTemplateModalTitle,
            deleteTemplateModalMessageTitle,noItemsMessage]).subscribe(
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
