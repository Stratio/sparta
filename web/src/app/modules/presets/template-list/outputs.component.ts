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
import { Store } from '@ngrx/store';

import { BreadcrumbMenuService } from 'services';
import * as fromRoot from 'reducers';
import * as outputActions from 'actions/output';

@Component({
    styleUrls: ['templates-base.styles.scss'],
    templateUrl: 'templates-base.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class OutputsComponent extends TemplatesBaseComponent {

    ngOnInit() {
        super.ngOnInit();

        this.templateListSubscription = this.store.select(fromRoot.getOutputList).subscribe((data: any) => {
            this.templateList = data;
            this._cd.detectChanges();
        });
        this.selectedDisplayOption$ = this.store.select(fromRoot.getSelectedOutputDisplayOption);
        this.selectedTemplatesSubscription = this.store.select(fromRoot.getSelectedOutputs).subscribe((data: any) => {
            this.selectedTemplates = data;
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

    // abstract method implementation
    changeDisplayOption(): void {
        this.store.dispatch(new outputActions.DisplayModeAction());
    }

    // abstract method implementation
    editTemplateAction(output: any) {
        this.store.dispatch(new outputActions.EditOutputAction(output));
    }

    constructor(private translate: TranslateService, store: Store<fromRoot.State>, modalService: StModalService,
        route: Router, currentActivatedRoute: ActivatedRoute, cd: ChangeDetectorRef,
        breadcrumbMenuService: BreadcrumbMenuService) {
        super(store, modalService, route, currentActivatedRoute, cd, breadcrumbMenuService);

        const deleteTemplateModalTitle = 'DASHBOARD.DELETE_OUTPUT_TITLE';
        const deleteTemplateModalMessage = 'DASHBOARD.DELETE_OUTPUT_MESSAGE';
        const duplicateTemplateModalTitle = 'DASHBOARD.DUPLICATE_OUTPUT';

        this.translate.get([deleteTemplateModalTitle, deleteTemplateModalMessage, duplicateTemplateModalTitle]).subscribe(
            (value: { [key: string]: string }) => {
                this.deleteTemplateModalTitle = value[deleteTemplateModalTitle];
                this.deleteTemplateModalMessage = value[deleteTemplateModalMessage];
                this.duplicateTemplateModalTitle = value[duplicateTemplateModalTitle];
            }
        );
    }
}
