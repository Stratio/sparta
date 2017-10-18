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

import { Component, OnInit, Output, EventEmitter, ViewChild, ChangeDetectionStrategy, OnDestroy } from '@angular/core';
import { NgForm, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { ActivatedRoute, Router } from '@angular/router';
import * as inputActions from 'actions/input';
import * as fromRoot from 'reducers';
import * as inputsTemplate from 'data-templates/inputs';
import { BreadcrumbMenuService, ErrorMessagesService, InitializeSchemaService } from 'services';
import { StDropDownMenuItem } from '@stratio/egeo';
import { CreateTemplateComponent } from './create-template.component';
import { Subscription } from 'rxjs/Rx';

@Component({
    selector: 'create-input',
    templateUrl: './create-template.template.html',
    styleUrls: ['./create-template.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CreateInputComponent extends CreateTemplateComponent implements OnDestroy {

    @Output() onCloseInputModal = new EventEmitter<string>();
    @ViewChild('inputForm') public inputForm: NgForm;
    public fragmentIndex = 0;
    public listData: any;
    public submitted = false;
    public fragmentName: any;
    public form: FormGroup;
    public fragmentTypes: StDropDownMenuItem[] = [];

    public configuration: FormGroup;
    public editMode = false;
    public title = '';
    public stepType = 'Input';
    private saveSubscription: Subscription;

    constructor(protected store: Store<fromRoot.State>, route: Router, errorsService: ErrorMessagesService,
        currentActivatedRoute: ActivatedRoute, formBuilder: FormBuilder, public breadcrumbMenuService: BreadcrumbMenuService,
        protected initializeSchemaService: InitializeSchemaService) {
        super(store, route, errorsService, currentActivatedRoute, formBuilder, breadcrumbMenuService, initializeSchemaService);
        this.store.dispatch(new inputActions.ResetInputFormAction());
        this.listData = inputsTemplate.inputs;

        this.fragmentTypes = this.listData.map((fragmentData: any) => {
            return {
                label: fragmentData.name,
                value: fragmentData.name
            }
        });

        this.saveSubscription = this.store.select(fromRoot.isInputSaved).subscribe((isSaved) => {
            if(isSaved) {
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
        if (this.inputForm.valid) {
            this.inputFormModel.templateType = 'input';
            if (this.editMode) {
                this.store.dispatch(new inputActions.UpdateInputAction(this.inputFormModel));
            } else {
                this.store.dispatch(new inputActions.CreateInputAction(this.inputFormModel));
            }
        }
    }

    getEditedTemplate() {
        this.store.select(fromRoot.getEditedInput).subscribe((editedInput: any) => {
            if (!editedInput.id) {
                return this.cancelCreate();
            }
            console.log(editedInput);
            this.setEditedTemplateIndex(editedInput.classPrettyName);
            this.inputFormModel = editedInput;
            this.breadcrumbOptions = this.breadcrumbMenuService.getOptions(editedInput.name);
        });
    }

    ngOnDestroy() {
        this.saveSubscription && this.saveSubscription.unsubscribe();
    }

}
