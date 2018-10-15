/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { OnInit, Output, EventEmitter, ViewChild } from '@angular/core';
import { NgForm, FormBuilder, FormGroup } from '@angular/forms';
import { Store } from '@ngrx/store';
import { ActivatedRoute, Router } from '@angular/router';
import { StDropDownMenuItem } from '@stratio/egeo';
import { HelpOptions } from '@app/shared/components/sp-help/sp-help.component';


import * as fromTemplates from './../../reducers';
import { BreadcrumbMenuService, ErrorMessagesService, InitializeSchemaService } from 'services';
import { Engine } from '@models/enums';

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
    public loaded = false;
    public isShowedHelp = false;

   public helpOptions: Array<HelpOptions> = [];

    constructor(protected store: Store<fromTemplates.State>,
        protected route: Router,
        public errorsService: ErrorMessagesService,
        private currentActivatedRoute: ActivatedRoute,
        private formBuilder: FormBuilder,
        public breadcrumbMenuService: BreadcrumbMenuService,
        protected initializeSchemaService: InitializeSchemaService) {
        this.breadcrumbOptions = [];
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
            const newModel = this.initializeSchemaService.setDefaultEntityModel(this.inputFormModel.executionEngine, this.listData[this.fragmentIndex], this.stepType);
            newModel.name = this.inputFormModel.name;
            newModel.description = this.inputFormModel.description;
            this.inputFormModel = newModel;
        }, 0);
        this.inputForm.form.markAsPristine();
    }

    changeFragment($event: any) {
    }

    closeHelp() {
      this.isShowedHelp = false;
   }

    ngOnInit() {
        //edition-mode
        if (this.route.url.indexOf('edit') > -1) {
            this.editMode = true;
            this.getEditedTemplate(this.currentActivatedRoute.snapshot.params.id);
        } else {
            this.breadcrumbOptions = this.breadcrumbMenuService.getOptions();
            this.inputFormModel.executionEngine = Engine.Streaming;
            this.inputFormModel = this.initializeSchemaService.setDefaultEntityModel(this.inputFormModel.executionEngine,
                this.listData[0], this.stepType);
            this.inputFormModel.classPrettyName = this.listData[this.fragmentIndex].classPrettyName;
            this.inputFormModel.description = '';
            this.inputFormModel.className = this.listData[this.fragmentIndex].className;
            this.loaded = true;
        }
        this.helpOptions = this.initializeSchemaService.getHelpOptions(this.listData[this.fragmentIndex].properties);
    }

    setEditedTemplateIndex(type: string) {
        for (let i = 0; i < this.listData.length; i++) {
            if (this.listData[i].classPrettyName === type) {
                this.fragmentIndex = i;
                this.inputFormModel.classPrettyName = type;
                this.inputFormModel.classPrettyName = this.listData[i].classPrettyName;
                this.inputFormModel.className = this.listData[i].className;
                this.inputForm.form.markAsPristine();
                return;
            }
        }
    }

    abstract getEditedTemplate(id: string): void;
}
