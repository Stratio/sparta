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
    Component, OnInit, OnDestroy, AfterViewChecked, ElementRef, ChangeDetectionStrategy,
    ChangeDetectorRef, HostListener, Input, ViewChild
} from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs/Rx';
import * as settingsTemplate from 'data-templates/settings.json';
import { Router, ActivatedRoute } from '@angular/router';
import * as wizardActions from 'actions/wizard';
import { BreadcrumbMenuService, ErrorMessagesService } from 'services';
import { inputs } from 'data-templates/inputs';
import { outputs } from 'data-templates/outputs';
import { transformations } from 'data-templates/transformations';
import { NgForm } from '@angular/forms';
import { StHorizontalTab } from '@stratio/egeo';


@Component({
    selector: 'wizard-config-editor',
    styleUrls: ['wizard-config-editor.styles.scss'],
    templateUrl: 'wizard-config-editor.template.html'
})

export class WizardConfigEditorComponent implements OnInit, OnDestroy {

    @Input() config: any;
    @ViewChild('entityForm') public entityForm: NgForm;

    public basicSettings: any = [];
    public advancedSettings: Array<any> = [];
    public submitted = false;
    public breadcrumbOptions: any = [];

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
            this.entityFormModel = Object.assign({}, this.config.editionType.data);
            this.breadcrumbOptions = this.breadcrumbMenuService.getOptions(this.config.editionType.data.name);
        } else {
            this.settingsSubscription = this.store.select(fromRoot.getWorkflowSettings).subscribe((settings: any) => {
                this.basicFormModel = settings.basic;
                this.settingsFormModel = settings.advancedSettings;
            });
        }
        switch (this.config.editionType.stepType) {
            case 'settings':
                const settings = <any>settingsTemplate;
                this.basicSettings = settings.basicSettings;
                this.advancedSettings = settings.advancedSettings;
                break;
            case 'Input':
                const selectedI: any = this._getEntityTemplate(inputs, this.config.editionType.data.classPrettyName);
                this.basicSettings = selectedI.properties;
                break;
            case 'Output':
                const selectedO: any = this._getEntityTemplate(outputs, this.config.editionType.data.classPrettyName);
                this.basicSettings = selectedO.properties;
                break;
            case 'Transformation':
                const selectedT: any = this._getEntityTemplate(transformations, this.config.editionType.data.classPrettyName);
                this.basicSettings = selectedT.properties;
                break;
        }
    }

    public saveForm() {
        this.submitted = true;
        if (this.config.editionType.stepType !== 'settings') {
            if (this.entityForm.valid) {
                this.store.dispatch(new wizardActions.SaveEntityAction({
                    oldName: this.config.editionType.data.name,
                    data: this.entityFormModel
                }));
            }
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

    private _getEntityTemplate(entityList: Array<any>, classPrettyName: string) {
        for (let i = 0; i < entityList.length; i++) {
            if (entityList[i].classPrettyName === classPrettyName) {
                return entityList[i];
            }
        }
    }


    constructor(private store: Store<fromRoot.State>, public breadcrumbMenuService: BreadcrumbMenuService,
        public errorsService: ErrorMessagesService) {
        this.breadcrumbOptions = breadcrumbMenuService.getOptions();
    }

    ngOnDestroy(): void {
        this.saveSubscription && this.saveSubscription.unsubscribe();
        this.settingsSubscription && this.settingsSubscription.unsubscribe();
    }

}
