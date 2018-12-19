/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, Output, EventEmitter, ViewChild, ChangeDetectionStrategy, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { NgForm, FormBuilder, FormGroup } from '@angular/forms';
import { Store, select } from '@ngrx/store';
import { ActivatedRoute, Router } from '@angular/router';
import { StDropDownMenuItem } from '@stratio/egeo';
import { Subscription } from 'rxjs';

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
    private selectedSubscription: Subscription;

    constructor(protected store: Store<fromTemplates.State>, route: Router, errorsService: ErrorMessagesService,
        currentActivatedRoute: ActivatedRoute, formBuilder: FormBuilder, public breadcrumbMenuService: BreadcrumbMenuService,
        protected initializeSchemaService: InitializeSchemaService, private _cd: ChangeDetectorRef) {
        super(store, route, errorsService, currentActivatedRoute, formBuilder, breadcrumbMenuService, initializeSchemaService);
        this.store.dispatch(new transformationActions.ResetTransformationFormAction());
        this.listData = transformationsTemplate.streamingTransformations;

        this.fragmentTypes = this.listData.map((fragmentData: any) => {
            return {
                label: fragmentData.classPrettyName,
                value: fragmentData.classPrettyName
            };
        });

        this.saveSubscription = this.store.pipe(select(fromTemplates.isTransformationSaved)).subscribe((isSaved) => {
            if (isSaved) {
                this.route.navigate(['templates', 'transformations']);
            }
        });
    }

    cancelCreate() {
        this.route.navigate(['templates', 'transformations']);
    }

    onSubmitInputForm(): void {
        this.submitted = true;
        if (this.transformationForm.valid) {
            this.inputFormModel.templateType = 'transformation';
            this.inputFormModel.supportedEngines = this.listData[this.fragmentIndex].supportedEngines;
            if (this.editMode) {
                this.store.dispatch(new transformationActions.UpdateTransformationAction(this.inputFormModel));
            } else {
                this.store.dispatch(new transformationActions.CreateTransformationAction(this.inputFormModel));
            }
        }
    }

    changeWorkflowType(event: any): void {
        this.inputFormModel.executionEngine = event.value;
        this.listData = event.value === 'Batch' ? transformationsTemplate.batchTransformations :
            transformationsTemplate.streamingTransformations;
        this.fragmentTypes = this.listData.map((fragmentData: any) => {
            return {
                label: fragmentData.classPrettyName,
                value: fragmentData.classPrettyName
            };
        });
        this.changeTemplateType(this.listData[0].name);
    }

    getEditedTemplate(templateId: string) {
        this.store.dispatch(new transformationActions.GetEditedTransformationAction(templateId));
        this.selectedSubscription = this.store.pipe(select(fromTemplates.getEditedTransformation)).subscribe((editedTransformation: any) => {
            if (!editedTransformation.id) {
                return;
            }
            this.inputFormModel.executionEngine = editedTransformation.executionEngine;
            this.listData = editedTransformation.executionEngine === 'Batch' ? transformationsTemplate.batchTransformations :
                transformationsTemplate.streamingTransformations;
            this.setEditedTemplateIndex(editedTransformation.classPrettyName);
            this.inputFormModel = editedTransformation;
            this.editedTemplateName = editedTransformation.name;
            const urlOptions = this.breadcrumbMenuService.getOptions(editedTransformation.name);
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
