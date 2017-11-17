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

import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TemplatesBaseComponent } from './templates-base.component';
import { TranslateService } from '@ngx-translate/core';
import { StModalService } from '@stratio/egeo';
import { ActivatedRoute, Router } from '@angular/router';
import { BreadcrumbMenuService } from 'services';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import * as transformationActions from 'actions/transformation';

@Component({
    styleUrls: ['templates-base.styles.scss'],
    templateUrl: 'templates-base.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class TransformationsComponent extends TemplatesBaseComponent {

    ngOnInit() {
        super.ngOnInit();
        this.templateListSubscription = this.store.select(fromRoot.getTransformationList).subscribe((data: any) => {
            this.templateList = data;
            this._cd.detectChanges();
        });

        this.selectedDisplayOption$ = this.store.select(fromRoot.getSelectedTransformationDisplayOption);

        this.selectedTemplatesSubscription = this.store.select(fromRoot.getSelectedTransformations).subscribe((data: any) => {
            this.selectedTemplates = data;
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


    constructor(private translate: TranslateService, protected store: Store<fromRoot.State>, modalService: StModalService,
        route: Router, currentActivatedRoute: ActivatedRoute, protected _cd: ChangeDetectorRef,
        breadcrumbMenuService: BreadcrumbMenuService) {
        super(store, modalService, route, currentActivatedRoute, _cd, breadcrumbMenuService);
        this.store.dispatch(new transformationActions.ResetTransformationFormAction());
        const deleteTemplateModalTitle = 'DASHBOARD.DELETE_TRANSFORMATION_TITLE';
        const deleteTemplateModalMessage = 'DASHBOARD.DELETE_TRANSFORMATION_MESSAGE';
        const duplicateTemplateModalTitle = 'DASHBOARD.DUPLICATE_TRANSFORMATION';

        this.translate.get([deleteTemplateModalTitle, deleteTemplateModalMessage, duplicateTemplateModalTitle]).subscribe(
            (value: { [key: string]: string }) => {
                this.deleteTemplateModalTitle = value[deleteTemplateModalTitle];
                this.deleteTemplateModalMessage = value[deleteTemplateModalMessage];
                this.duplicateTemplateModalTitle = value[duplicateTemplateModalTitle];
            }
        );
    }
}
