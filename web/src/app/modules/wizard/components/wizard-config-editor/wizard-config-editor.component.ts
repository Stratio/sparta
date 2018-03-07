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

import {
    Component, OnInit, OnDestroy, ChangeDetectionStrategy, Input, ViewChild
} from '@angular/core';
import { Store } from '@ngrx/store';
import { NgForm } from '@angular/forms';
import { StHorizontalTab } from '@stratio/egeo';
import * as wizardActions from 'actions/wizard';
import { Router } from '@angular/router';

import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs/Subscription';
import { ErrorMessagesService } from 'services';
import { writerTemplate } from 'data-templates/index';
import { WizardService } from '@app/wizard/services/wizard.service';

@Component({
    selector: 'wizard-config-editor',
    styleUrls: ['wizard-config-editor.styles.scss'],
    templateUrl: 'wizard-config-editor.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardConfigEditorComponent implements OnInit, OnDestroy {

    @Input() config: any;
    @Input() workflowType: string;
    @ViewChild('entityForm') public entityForm: NgForm;

    public basicSettings: any = [];
    public writerSettings: any = [];
    public submitted = true;
    public currentName = '';
    public arity: any;

    public isTemplate = false;
    public templateData: any;

    public validatedName = false;
    public basicFormModel: any = {};    // inputs, outputs, transformation basic settings (name, description)
    public entityFormModel: any = {};   // common config

    public activeOption = 'Global';
    public options: StHorizontalTab[] = [{
        id: 'Global',
        text: 'Global'
    }, {
        id: 'Writer',
        text: 'Writer'
    }];

    private saveSubscription: Subscription;
    private validatedNameSubcription: Subscription;
    ngOnInit(): void {
        this.validatedNameSubcription = this.store.select(fromRoot.getValidatedEntityName).subscribe((validation: boolean) => {
            this.validatedName = validation;
        });

        this.saveSubscription = this.store.select(fromRoot.isEntitySaved).subscribe((isEntitySaved) => {
            if (isEntitySaved) {
                // hide edition when its saved
                this.store.dispatch(new wizardActions.HideEditorConfigAction());
            }
        });
        this.getFormTemplate();
    }

    resetValidation() {
        this.store.dispatch(new wizardActions.SaveEntityErrorAction(false));
    }

    cancelEdition() {
        this.store.dispatch(new wizardActions.HideEditorConfigAction());
    }

    changeFormOption($event: any) {
        this.activeOption = $event.id;
    }

    editTemplate(templateId) {
        const ask = window.confirm('If you leave this page you will lose the unsaved changes of the workflow');
        if (ask) {
            this._router.navigate(['templates', 'inputs', 'edit', templateId]);
        }

    }

    getFormTemplate() {
        if (this.config.editionType.data.createdNew) {
            this.submitted = false;
        }
        this.entityFormModel = Object.assign({}, this.config.editionType.data);
        this.currentName = this.entityFormModel['name'];
        let template: any;
        switch (this.config.editionType.stepType) {
            case 'Input':
                template = this._wizardService.getInputs()[this.config.editionType.data.classPrettyName];
                this.writerSettings = writerTemplate;
                break;
            case 'Output':
                template = this._wizardService.getOutputs()[this.config.editionType.data.classPrettyName];
                break;
            case 'Transformation':
                template = this._wizardService.getTransformations()[this.config.editionType.data.classPrettyName];
                this.writerSettings = writerTemplate;
                break;
        }
        this.basicSettings = template.properties;
        if (this.entityFormModel.nodeTemplate && this.entityFormModel.nodeTemplate.id && this.entityFormModel.nodeTemplate.id.length) {
            this.isTemplate = true;
            const nodeTemplate = this.entityFormModel.nodeTemplate;
            this.store.select(fromRoot.getTemplates).take(1).subscribe((templates: any) => {
                this.templateData = templates[this.config.editionType.stepType.toLowerCase()]
                    .find((templateD: any) => templateD.id === nodeTemplate.id);
            });
        } else {
            this.isTemplate = false;
            if (template.arity) {
                this.arity = template.arity;
            }
        }
    }

    public saveForm() {
        this.entityFormModel.hasErrors = this.entityForm.invalid;
        if (this.arity) {
            this.entityFormModel.arity = this.arity;
        }
        if (this.isTemplate) {
            this.entityFormModel.configuration = this.templateData.configuration;
        }
        this.entityFormModel.createdNew = false;
        this.store.dispatch(new wizardActions.SaveEntityAction({
            oldName: this.config.editionType.data.name,
            data: this.entityFormModel
        }));
    }

    constructor(private store: Store<fromRoot.State>,
        private _router: Router,
        private _wizardService: WizardService,
        public errorsService: ErrorMessagesService) {
    }

    ngOnDestroy(): void {
        this.saveSubscription && this.saveSubscription.unsubscribe();
        this.validatedNameSubcription && this.validatedNameSubcription.unsubscribe();
    }

}