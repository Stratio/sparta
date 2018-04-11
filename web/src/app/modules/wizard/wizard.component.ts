/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import * as fromWizard from './reducers';
import * as wizardActions from './actions/wizard';
import { WizardService } from './services/wizard.service';


@Component({
    selector: 'wizard',
    styleUrls: ['wizard.styles.scss'],
    templateUrl: 'wizard.template.html'
})

export class WizardComponent implements OnInit, OnDestroy {

    public workflowType = 'Streaming';

    public creationMode$: Observable<any>;
    public editionConfigMode$: Observable<any>;
    public showSettings$: Observable<boolean>;

    private _paramSubscription: Subscription;
    private _saveSubscription: Subscription;
    private _workflowTypeSubscription: Subscription;

    public isEdit = false;

    constructor(
        private _store: Store<fromWizard.State>,
        private _route: ActivatedRoute,
        private _wizardService: WizardService) {
        const id = this._route.snapshot.params.id;

        if (id && id.length) {
            this.isEdit = true;
        }

        this._store.dispatch(new wizardActions.ResetWizardAction(this.isEdit)); // Reset wizard to default settings
        const type = this._route.snapshot.params.type === 'streaming' ? 'Streaming' : 'Batch';

        if (this.isEdit) {
            this._store.dispatch(new wizardActions.ModifyWorkflowAction(id));
        } else {
            this._wizardService.workflowType = type;
            this._store.dispatch(new wizardActions.SetWorkflowTypeAction(type));
            this._store.dispatch(new wizardActions.GetMenuTemplatesAction());
        }

        this._workflowTypeSubscription = this._store.select(fromWizard.getWorkflowType).subscribe((workflowType: string) => {
            this._wizardService.workflowType = workflowType;
            this.workflowType = workflowType;
        });
    }

    ngOnInit(): void {
        this.creationMode$ = this._store.select(fromWizard.isCreationMode);               // show create node pointer icon
        this.editionConfigMode$ = this._store.select(fromWizard.getEditionConfigMode);    // show node/settings editor view
        this.showSettings$ = this._store.select(fromWizard.showSettings);
    }

    ngOnDestroy(): void {
        this._paramSubscription && this._paramSubscription.unsubscribe();
        this._saveSubscription && this._saveSubscription.unsubscribe();
        this._workflowTypeSubscription && this._workflowTypeSubscription.unsubscribe();
    }
}
