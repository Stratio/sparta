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
import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs/Subscription';
import * as wizardActions from 'actions/wizard';
import { BreadcrumbMenuService, ErrorMessagesService } from 'services';
import { settingsTemplate, writerTemplate } from 'data-templates/index';
import { NgForm } from '@angular/forms';
import { StHorizontalTab } from '@stratio/egeo';
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
    public advancedSettings: Array<any> = [];
    public submitted = true;
    public breadcrumbOptions: any = [];
    public currentName = '';
    public arity: any;
    public settingsForm = false;

    public basicFormModel: any = {};    // inputs, outputs, transformation basic settings (name, description)
    public entityFormModel: any = {};   // common config
    public settingsFormModel: any = {}; // advanced settings

    public activeOption = 'Global';
    public options: StHorizontalTab[] = [{
        id: 'Global',
        text: 'Global'
    }, {
        id: 'Writer',
        text: 'Writer'
    }];

    private saveSubscription: Subscription;
    private settingsSubscription: Subscription;

    ngOnInit(): void {
        this.store.select(fromRoot.isEntitySaved).subscribe((isEntitySaved) => {
            if (isEntitySaved) {
                // hide edition when its saved
                this.store.dispatch(new wizardActions.HideEditorConfigAction());
            }
        });

        this.getFormTemplate();
    }

    cancelEdition() {
        this.store.dispatch(new wizardActions.HideEditorConfigAction());
    }

    changeFormOption($event: any) {
        this.activeOption = $event.id;
    }

    getFormTemplate() {
        if (this.config.editionType.stepType !== 'settings') {
            if (this.config.editionType.data.createdNew) {
                this.submitted = false;
            }
            this.entityFormModel = Object.assign({}, this.config.editionType.data);
            this.currentName = this.entityFormModel['name'];
            this.breadcrumbOptions = this.breadcrumbMenuService.getOptions(this.config.editionType.data.name);
        } else {
            this.settingsForm = true;
            this.settingsSubscription = this.store.select(fromRoot.getWorkflowSettings).subscribe((settings: any) => {
                this.basicFormModel = settings.basic;
                this.settingsFormModel = settings.advancedSettings;
            });
        }
        let template: any;
        switch (this.config.editionType.stepType) {
            case 'settings':
                const settings = <any>settingsTemplate;
                this.basicSettings = settings.basicSettings;
                this.advancedSettings = settings.advancedSettings;
                return;
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
        if (template.arity) {
            this.arity = template.arity;
        }
    }

    public saveForm() {
        if (this.config.editionType.stepType !== 'settings') {

            this.entityFormModel.hasErrors = this.entityForm.invalid;
            if (this.arity) {
                this.entityFormModel.arity = this.arity;
            }
            this.entityFormModel.createdNew = false;
            this.store.dispatch(new wizardActions.SaveEntityAction({
                oldName: this.config.editionType.data.name,
                data: this.entityFormModel
            }));

        } else {
            if (this.entityForm.valid) {
                this.store.dispatch(new wizardActions.SaveSettingsAction({
                    basic: this.basicFormModel,
                    advancedSettings: this.settingsFormModel
                }));
                this.store.dispatch(new wizardActions.HideEditorConfigAction());
            }
        }
    }


    constructor(private store: Store<fromRoot.State>,
        private _wizardService: WizardService,
        public breadcrumbMenuService: BreadcrumbMenuService,
        public errorsService: ErrorMessagesService) {
        this.breadcrumbOptions = breadcrumbMenuService.getOptions();
    }

    ngOnDestroy(): void {
        this.saveSubscription && this.saveSubscription.unsubscribe();
        this.settingsSubscription && this.settingsSubscription.unsubscribe();
    }

}
