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
import { Subscription } from 'rxjs/Subscription';

import * as fromRoot from 'reducers';
import * as wizardActions from 'actions/wizard';
import { settingsTemplate } from 'data-templates/index';
import { WizardService } from '@app/wizard/services/wizard.service';


@Component({
    selector: 'wizard-settings',
    styleUrls: ['wizard-settings.styles.scss'],
    templateUrl: 'wizard-settings.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardSettingsComponent implements OnInit, OnDestroy {

    @Input() edition: boolean;
    @Input() workflowType: string;
    @ViewChild('entityForm') public entityForm: NgForm;

    public basicSettings: any = [];
    public advancedSettings: Array<any> = [];
    public submitted = true;
    public currentName = '';
    public tags: Array<string> = [];

    public basicFormModel: any = {};    // inputs, outputs, transformation basic settings (name, description)
    public entityFormModel: any = {};   // common config
    public settingsFormModel: any = {}; // advanced settings

    private errorManagementOutputs: Array<string> = [];
    private settingsSubscription: Subscription;

    ngOnInit(): void {
        // get all error management outputs
        this.store.select(fromRoot.getErrorsManagementOutputs).take(1).subscribe((errorManagementOutputs: Array<string>) => {
            this.errorManagementOutputs = errorManagementOutputs;
        });
        this.getFormTemplate();
    }

    cancelEdition() {
        this.store.dispatch(new wizardActions.HideSettingsAction());
    }

    getFormTemplate() {
        this.settingsSubscription = this.store.select(fromRoot.getWorkflowSettings).subscribe((currentSettings: any) => {
            const settings = JSON.parse(JSON.stringify(currentSettings));
            if (settings.basic.tag) {
                this.tags = settings.basic.tag.split(',');
            }

            this.basicFormModel = settings.basic;
            this.settingsFormModel = settings.advancedSettings;
        });

        const settings = <any>settingsTemplate;

        // hides workflow name edition when its in edit mode
        this.basicSettings = this.edition ? settings.basicSettings.map((field: any) => {
            return field.propertyId === 'name' ? Object.assign({}, field, {
                hidden: true
            }) : field;
        }) : settings.basicSettings;
        settings.advancedSettings[3].properties[2].properties[0].fields[0].values = this.errorManagementOutputs.map((outputName: string) => {
            return {
                label: outputName,
                value: outputName
            }
        });

        this.advancedSettings = this.workflowType === 'Streaming' ? settings.advancedSettings :
            settings.advancedSettings.filter((section: any) => {
                return section.name !== 'streamingSettings';
            });
    }

    public saveForm() {
        if (this.entityForm.valid) {
            this.basicFormModel.tag = this.tags.join(',');
            this.store.dispatch(new wizardActions.SaveSettingsAction({
                basic: this.basicFormModel,
                advancedSettings: this.settingsFormModel
            }));
            this.store.dispatch(new wizardActions.ValidateWorkflowAction());
            this.store.dispatch(new wizardActions.HideSettingsAction());
        }
    }


    constructor(private store: Store<fromRoot.State>,
        private _wizardService: WizardService) {
    }

    ngOnDestroy(): void {
        this.settingsSubscription && this.settingsSubscription.unsubscribe();
    }

}