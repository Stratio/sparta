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

import { OnInit, Output, EventEmitter, ViewChild } from '@angular/core';
import { NgForm, FormBuilder, FormGroup } from '@angular/forms';
import { Store } from '@ngrx/store';
import { ActivatedRoute, Router } from '@angular/router';
import * as fromRoot from 'reducers';
import { BreadcrumbMenuService, ErrorMessagesService, InitializeSchemaService } from 'services';
import { StDropDownMenuItem } from '@stratio/egeo';

export abstract class CreateTemplateComponent implements OnInit {

    @Output() onCloseInputModal = new EventEmitter<string>();
    @ViewChild('inputForm') public inputForm: NgForm;

    public fragmentIndex = 0;
    public listData: any;
    public submitted = false;
    public fragmentName: any;
    public form: FormGroup;
    public stepType = '';
    public fragmentTypes: StDropDownMenuItem[] = [];
    public breadcrumbOptions: string[] = [];
    public inputFormModel: any = {
        name: '',
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
        public breadcrumbMenuService: BreadcrumbMenuService,
        protected initializeSchemaService: InitializeSchemaService) {

        this.breadcrumbOptions = breadcrumbMenuService.getOptions();
        this.configuration = new FormGroup({});
    }


    changeFragmentIndex(index: number): void {
        //this.inputFormModel.element.configuration = {};
        this.fragmentIndex = index;
    }

    changeTemplateType(event: string): void {
        this.inputFormModel.classPrettyName = event;
        this.setEditedTemplateIndex(event);
        setTimeout(() => { // set default model and remove description
            this.inputFormModel = this.initializeSchemaService.setDefaultEntityModel(this.listData[this.fragmentIndex], this.stepType);
            this.inputFormModel.description = '';
        }, 0);
        this.inputForm.form.markAsPristine();
    }

    cancelCreate() {
        this.route.navigate(['..'], { relativeTo: this.currentActivatedRoute });
    }

    changeFragment($event: any) {
    }

    ngOnInit() {
        //edition-mode
        if (this.route.url.indexOf('edit') > -1) {
            this.editMode = true;
            this.getEditedTemplate();
        } else {
            this.inputFormModel = this.initializeSchemaService.setDefaultEntityModel(this.listData[this.fragmentIndex], this.stepType);
            this.inputFormModel.classPrettyName = this.listData[this.fragmentIndex].classPrettyName;
            this.inputFormModel.description = '';
            this.inputFormModel.className = this.listData[this.fragmentIndex].className;
        }
    }

    setEditedTemplateIndex(type: string) {
        for (let i = 0; i < this.listData.length; i++) {
            if (this.listData[i].classPrettyName === type) {
                this.fragmentIndex = i;
                this.inputFormModel.classPrettyName = type;
                this.inputFormModel.className = this.listData[i].className;
                this.inputForm.form.markAsPristine();
                return;
            }
        }
    }

    abstract getEditedTemplate(): void;
}
