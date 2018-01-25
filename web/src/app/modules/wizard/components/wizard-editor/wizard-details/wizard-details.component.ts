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

import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs/Rx';
import { WizardService } from '@app/wizard/services/wizard.service';
import * as fromRoot from 'reducers';


@Component({
    selector: 'wizard-details',
    templateUrl: './wizard-details.template.html',
    styleUrls: ['./wizard-details.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WizardDetailsComponent implements OnInit, OnDestroy {

    @Input() entityData: any;

    private _workflowTypeSubscription: Subscription;
    public templates: any = {};

    constructor(private _cd: ChangeDetectorRef, private wizardService: WizardService, private _store: Store<fromRoot.State>, ) { }

    ngOnInit() {
        this._workflowTypeSubscription = this._store.select(fromRoot.getWorkflowType).subscribe((workflowType: string) => {
            this.wizardService.workflowType = workflowType;
            this.templates = {
                Input: this.wizardService.getInputs(),
                Output: this.wizardService.getOutputs(),
                Transformation: this.wizardService.getTransformations(),
            };
            this._cd.detectChanges();
        });

    }

    ngOnDestroy(): void {
        this._workflowTypeSubscription && this._workflowTypeSubscription.unsubscribe();
    }
}
