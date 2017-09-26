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

import { Component, OnInit, Output, EventEmitter, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { NgForm, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { ActivatedRoute, Router } from '@angular/router';
import * as inputActions from 'actions/input';
import * as fromRoot from 'reducers';
import * as outputsTemplate from 'data-templates/outputs';
import { BreadcrumbMenuService, ErrorMessagesService } from 'services';
import { StDropDownMenuItem } from '@stratio/egeo';
import { CreateTemplateComponent } from './create-template.component';

@Component({
    selector: 'create-output',
    templateUrl: './create-template.template.html',
    styleUrls: ['./create-template.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CreateOutputComponent extends CreateTemplateComponent {

    @Output() onCloseInputModal = new EventEmitter<string>();
    @ViewChild('inputForm') public inputForm: NgForm;
    public fragmentIndex = 0;
    public listData: any;
    public submitted = false;
    public fragmentName: any;
    public form: FormGroup;
    public fragmentTypes: StDropDownMenuItem[] = [];
    public inputFormModel: any = {
        element: {
        }
    };
    public configuration: FormGroup;
    public editMode = false;

    constructor(protected store: Store<fromRoot.State>, route: Router, errorsService: ErrorMessagesService,
        currentActivatedRoute: ActivatedRoute, formBuilder: FormBuilder, breadcrumbMenuService: BreadcrumbMenuService) {
        super(store, route, errorsService, currentActivatedRoute, formBuilder, breadcrumbMenuService);

        this.listData = outputsTemplate.outputs;

        this.fragmentTypes = this.listData.map((fragmentData: any) => {
            return {
                label: fragmentData.name,
                value: fragmentData.name
            }
        });
    }

    changeFragmentIndex(index: number): void {
        this.inputFormModel.element.configuration = {};
        this.fragmentIndex = index;
    }


    onSubmitInputForm(): void {
        this.submitted = true;
        if (this.inputForm.valid) {
            this.inputFormModel.templateType = 'output';

            if (this.editMode) {
                this.store.dispatch(new inputActions.UpdateInputAction(this.inputFormModel));
            } else {
                this.store.dispatch(new inputActions.CreateInputAction(this.inputFormModel));
            }
        }
    }

    changeFragment($event: any) {
        console.log($event);
    }

    getEditedTemplate() {
        this.store.select(fromRoot.getEditedOutput).subscribe((editedOutput: any) => {
            if (!editedOutput.id) {
                return this.cancelCreate();
            }
            this.setEditedTemplateIndex(editedOutput.type || 'kafka');
            this.inputFormModel = editedOutput;
        });
    }
}
