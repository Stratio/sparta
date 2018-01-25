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

import { Component, Output, EventEmitter, ViewChild, ChangeDetectionStrategy, OnDestroy } from '@angular/core';
import { NgForm, FormBuilder, FormGroup } from '@angular/forms';
import { Store } from '@ngrx/store';
import { ActivatedRoute, Router } from '@angular/router';
import { StDropDownMenuItem } from '@stratio/egeo';
import { Subscription } from 'rxjs/Rx';

import * as transformationActions from './../../actions/transformation';
import * as fromTemplates from './../../reducers';
import * as transformationsTemplate from 'data-templates/transformations';
import { BreadcrumbMenuService, ErrorMessagesService, InitializeSchemaService } from 'services';
import { CreateTemplateComponent } from './create-template.component';

@Component({
    selector: 'create-transformation',
    templateUrl: './create-template.template.html',
    styleUrls: ['./create-template.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CreateTransformationsComponent extends CreateTemplateComponent implements OnDestroy {

    @Output() onCloseTransformationModal = new EventEmitter<string>();
    @ViewChild('inputForm') public transformationForm: NgForm;
    public fragmentIndex = 0;
    public listData: any;
    public submitted = false;
    public fragmentName: any;
    public form: FormGroup;
    public fragmentTypes: StDropDownMenuItem[] = [];

    public configuration: FormGroup;
    public editMode = false;
    public title = '';
    public stepType = 'Transformation';
    public stepKey = 'TRANSFORMATION';
    public editedTemplateName = '';

    private saveSubscription: Subscription;

    constructor(protected store: Store<fromTemplates.State>, route: Router, errorsService: ErrorMessagesService,
        currentActivatedRoute: ActivatedRoute, formBuilder: FormBuilder, public breadcrumbMenuService: BreadcrumbMenuService,
        protected initializeSchemaService: InitializeSchemaService) {
        super(store, route, errorsService, currentActivatedRoute, formBuilder, breadcrumbMenuService, initializeSchemaService);
        this.store.dispatch(new transformationActions.ResetTransformationFormAction());
        this.listData = transformationsTemplate.transformations;

        this.fragmentTypes = this.listData.map((fragmentData: any) => {
            return {
                label: fragmentData.name,
                value: fragmentData.name
            };
        });

        this.saveSubscription = this.store.select(fromTemplates.isTransformationSaved).subscribe((isSaved) => {
            if (isSaved) {
                this.route.navigate(['..'], { relativeTo: currentActivatedRoute });
            }
        });
    }


    changeFragmentIndex(index: number): void {
        this.submitted = false;
        this.inputFormModel.element.configuration = {};
        this.fragmentIndex = index;
    }

    onSubmitInputForm(): void {
        this.submitted = true;
        if (this.transformationForm.valid) {
            this.inputFormModel.templateType = 'transformation';
            if (this.editMode) {
                this.store.dispatch(new transformationActions.UpdateTransformationAction(this.inputFormModel));
            } else {
                this.store.dispatch(new transformationActions.CreateTransformationAction(this.inputFormModel));
            }
        }
    }

    getEditedTemplate() {
        this.store.select(fromTemplates.getEditedTransformation).subscribe((editedTransformation: any) => {
            if (!editedTransformation.id) {
                return this.cancelCreate();
            }
            this.setEditedTemplateIndex(editedTransformation.classPrettyName);
            this.inputFormModel = editedTransformation;
            this.editedTemplateName = editedTransformation.name;
            this.breadcrumbOptions = this.breadcrumbMenuService.getOptions(editedTransformation.name);
        });
    }

    ngOnDestroy() {
        this.saveSubscription && this.saveSubscription.unsubscribe();
    }

}
