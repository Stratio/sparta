/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs/Rx';
import { WizardService } from '@app/wizard/services/wizard.service';

import * as fromWizard from './../../reducers';

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

    constructor(private _cd: ChangeDetectorRef, private wizardService: WizardService, private _store: Store<fromWizard.State>, ) { }

    ngOnInit() {
        this._workflowTypeSubscription = this._store.select(fromWizard.getWorkflowType).subscribe((workflowType: string) => {
            this.wizardService.workflowType = workflowType;
            this.templates = {
                Input: this.wizardService.getInputs(),
                Output: this.wizardService.getOutputs(),
                Transformation: this.wizardService.getTransformations(),
            };
            this._cd.markForCheck();
        });

    }

    ngOnDestroy(): void {
        this._workflowTypeSubscription && this._workflowTypeSubscription.unsubscribe();
    }
}
