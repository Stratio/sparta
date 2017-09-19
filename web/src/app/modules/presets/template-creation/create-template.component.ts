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
import * as inputTemplate from 'data-templates/input.json';
import { BreadcrumbMenuService, ErrorMessagesService } from 'services';
import { StDropDownMenuItem } from '@stratio/egeo';

export abstract class CreateTemplateComponent implements OnInit {

    @Output() onCloseInputModal = new EventEmitter<string>();
    @ViewChild('inputForm') public inputForm: NgForm;
    public fragmentIndex = 0;
    public listData: any;
    public submitted = false;
    public fragmentName: any;
    public form: FormGroup;
    public fragmentTypes: StDropDownMenuItem[] = [];
    public breadcrumbOptions: string[] = [];
    public inputFormModel: any = {
        element: {
        }
    };
    public configuration: FormGroup;
    public editMode = false;

    constructor(protected store: Store<fromRoot.State>,
        protected route: Router,
        public errorsService: ErrorMessagesService,
        private currentActivatedRoute: ActivatedRoute,
        private formBuilder: FormBuilder,
        public breadcrumbMenuService: BreadcrumbMenuService) {

        this.breadcrumbOptions = breadcrumbMenuService.getOptions();
        this.configuration = new FormGroup({});
    }


    changeFragmentIndex(index: number): void {
        this.inputFormModel.element.configuration = {};
        this.fragmentIndex = index;
    }

    cancelCreate() {
        this.route.navigate(['..'], { relativeTo: this.currentActivatedRoute });
    }

    onSubmitInputForm(): void {
        this.submitted = true;
        if (this.inputForm.valid) {
            this.setInputModel();
            if (this.editMode) {
                this.store.dispatch(new inputActions.UpdateInputAction(this.inputFormModel));
            } else {
                this.store.dispatch(new inputActions.CreateInputAction(this.inputFormModel));
            }
        }
    }

    setInputModel() {
        let fragment = this.listData[this.fragmentIndex];

        this.inputFormModel.description = fragment.description.long;
        this.inputFormModel.shortDescription = fragment.description.short;
        this.inputFormModel.fragmentType = 'input';
        this.inputFormModel.element.name = fragment.name;
        this.inputFormModel.element.type = fragment.modelType;
    }

    changeFragment($event: any) {
        console.log($event);
    }

    ngOnInit() {
        //edition-mode
        if (this.route.url.indexOf('edit') > -1) {
            this.editMode = true;
            this.getEditedTemplate();
        } else {
            this.inputFormModel.templateType = this.fragmentTypes[0];
        }
    }

    setEditedTemplateIndex(templateType: string) {
        for (let i = 0; i < this.listData.length; i++) {
            if (this.listData[i].modelType === templateType) {
                return;
            }
        }
    }

    abstract getEditedTemplate(): void;
}
