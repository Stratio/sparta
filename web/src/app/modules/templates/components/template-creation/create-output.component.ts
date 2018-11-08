/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, Output, EventEmitter, ViewChild, ChangeDetectionStrategy, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { NgForm, FormBuilder, FormGroup } from '@angular/forms';
import { Store } from '@ngrx/store';
import { ActivatedRoute, Router } from '@angular/router';
import { StDropDownMenuItem } from '@stratio/egeo';
import { Subscription } from 'rxjs/Subscription';

import * as outputActions from './../../actions/output';
import * as fromTemplates from './../../reducers';
import * as outputsTemplate from 'data-templates/outputs';
import { BreadcrumbMenuService, ErrorMessagesService, InitializeSchemaService } from 'services';
import { CreateTemplateComponent } from './create-template.component';
import { Engine } from '@models/enums';

@Component({
    selector: 'create-output',
    templateUrl: './create-template.template.html',
    styleUrls: ['./create-template.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CreateOutputComponent extends CreateTemplateComponent implements OnDestroy {

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
    public stepType = 'Output';
    public stepKey = 'OUTPUT';
    public editedTemplateName = '';

    private saveSubscription: Subscription;
    private selectedSubscription: Subscription;

    constructor(protected store: Store<fromTemplates.State>, route: Router, errorsService: ErrorMessagesService,
        currentActivatedRoute: ActivatedRoute, formBuilder: FormBuilder, public breadcrumbMenuService: BreadcrumbMenuService,
        protected initializeSchemaService: InitializeSchemaService, private _cd: ChangeDetectorRef) {
        super(store, route, errorsService, currentActivatedRoute, formBuilder, breadcrumbMenuService, initializeSchemaService);
        this.store.dispatch(new outputActions.ResetOutputFormAction());
        this.listData = outputsTemplate.streamingOutputs;

        this.fragmentTypes = this.listData.map((fragmentData: any) => {
            return {
                label: fragmentData.classPrettyName,
                value: fragmentData.classPrettyName
            };
        });

        this.saveSubscription = this.store.select(fromTemplates.isOutputSaved).subscribe((isSaved) => {
            if (isSaved) {
                this.route.navigate(['templates', 'outputs']);
            }
        });
    }


    cancelCreate() {
        this.route.navigate(['templates', 'outputs']);
    }

    onSubmitInputForm(): void {
        this.submitted = true;
        if (this.inputForm.valid) {
            this.inputFormModel.templateType = 'output';
            this.inputFormModel.supportedEngines = this.listData[this.fragmentIndex].supportedEngines;
            if (this.editMode) {
                this.store.dispatch(new outputActions.UpdateOutputAction(this.inputFormModel));
            } else {
                this.store.dispatch(new outputActions.CreateOutputAction(this.inputFormModel));
            }
        }
    }

    changeWorkflowType(event: any): void {
        this.inputFormModel.executionEngine = event.value;
        this.listData = event.value === Engine.Batch ? outputsTemplate.batchOutputs : outputsTemplate.streamingOutputs;
        this.changeTemplateType(this.listData[0].name);
        this.fragmentTypes = this.listData.map((fragmentData: any) => {
            return {
                label: fragmentData.classPrettyName,
                value: fragmentData.classPrettyName
            };
        });
    }

    getEditedTemplate(templateId: string) {
        this.store.dispatch(new outputActions.GetEditedOutputAction(templateId));
        this.selectedSubscription = this.store.select(fromTemplates.getEditedOutput).subscribe((editedOutput: any) => {
            if (!editedOutput.id) {
                return;
            }
            this.inputFormModel.executionEngine = editedOutput.executionEngine;
            this.listData = editedOutput.executionEngine === Engine.Batch ? outputsTemplate.batchOutputs :
                outputsTemplate.streamingOutputs;
            this.setEditedTemplateIndex(editedOutput.classPrettyName);
            this.inputFormModel = editedOutput;
            this.editedTemplateName = editedOutput.name;
            const urlOptions = this.breadcrumbMenuService.getOptions(editedOutput.name);
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
