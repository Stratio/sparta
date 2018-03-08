/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, Output, EventEmitter, ViewChild, ChangeDetectionStrategy, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { NgForm, FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { Store } from '@ngrx/store';
import { StDropDownMenuItem } from '@stratio/egeo';

import * as inputActions from './../../actions/input';
import * as fromTemplates from './../../reducers';
import * as inputsTemplate from 'data-templates/inputs';
import { BreadcrumbMenuService, ErrorMessagesService, InitializeSchemaService } from 'services';
import { CreateTemplateComponent } from './create-template.component';


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
    public stepKey = 'INPUT';
    public editedTemplateName = '';

    private saveSubscription: Subscription;
    private selectedSubscription: Subscription;

    constructor(protected store: Store<fromTemplates.State>, route: Router, errorsService: ErrorMessagesService,
        currentActivatedRoute: ActivatedRoute, formBuilder: FormBuilder, public breadcrumbMenuService: BreadcrumbMenuService,
        protected initializeSchemaService: InitializeSchemaService, private _cd: ChangeDetectorRef) {
        super(store, route, errorsService, currentActivatedRoute, formBuilder, breadcrumbMenuService, initializeSchemaService);
        this.store.dispatch(new inputActions.ResetInputFormAction());
        this.listData = inputsTemplate.streamingInputs;

        this.fragmentTypes = this.listData.map((fragmentData: any) => {
            return {
                label: fragmentData.name,
                value: fragmentData.name
            };
        });

        this.saveSubscription = this.store.select(fromTemplates.isInputSaved).subscribe((isSaved) => {
            if (isSaved) {
                this.route.navigate(['templates', 'inputs']);
            }
        });
    }

    cancelCreate() {
        this.route.navigate(['templates', 'inputs']);
    }

    onSubmitInputForm(): void {
        this.submitted = true;
        if (this.inputForm.valid) {
            this.inputFormModel.templateType = 'input';
            this.inputFormModel.supportedEngines = this.listData[this.fragmentIndex].supportedEngines;
            if (this.editMode) {
                this.store.dispatch(new inputActions.UpdateInputAction(this.inputFormModel));
            } else {
                this.store.dispatch(new inputActions.CreateInputAction(this.inputFormModel));
            }
        }
    }

    changeWorkflowType(event: any): void {
        this.inputFormModel.executionEngine = event.value;
        this.listData = event.value === 'Batch' ? inputsTemplate.batchInputs : inputsTemplate.streamingInputs;
        this.changeTemplateType(this.listData[0].name);
        this.fragmentTypes = this.listData.map((fragmentData: any) => {
            return {
                label: fragmentData.name,
                value: fragmentData.name
            };
        });
    }

    getEditedTemplate(templateId: string) {
        this.store.dispatch(new inputActions.GetEditedInputAction(templateId));
        this.selectedSubscription = this.store.select(fromTemplates.getEditedInput).subscribe((editedInput: any) => {
            if (!editedInput.id) {
                return;
            }
            this.listData = editedInput.executionEngine === 'Batch' ? inputsTemplate.batchInputs : inputsTemplate.streamingInputs;
            this.setEditedTemplateIndex(editedInput.classPrettyName);
            this.inputFormModel = editedInput;
            this.editedTemplateName = editedInput.name;
            const urlOptions = this.breadcrumbMenuService.getOptions(editedInput.name);
            this.breadcrumbOptions = urlOptions.filter(option => option !== 'edit');
            this._cd.markForCheck();

            setTimeout(() => {
                this.loaded = true;
                this._cd.markForCheck();
            });

        });
    }

    ngOnDestroy() {
        this.saveSubscription && this.saveSubscription.unsubscribe();
        this.selectedSubscription && this.selectedSubscription.unsubscribe();
    }

}
