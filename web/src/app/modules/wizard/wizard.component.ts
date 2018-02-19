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

import { Component, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subscription } from 'rxjs/Rx';
import { Location } from '@angular/common';

import * as fromRoot from 'reducers';
import * as wizardActions from 'actions/wizard';
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
        private _cd: ChangeDetectorRef,
        private _store: Store<fromRoot.State>,
        private _translate: TranslateService,
        private _router: Router,
        private _route: ActivatedRoute,
        private _wizardService: WizardService,
        private _location: Location) {

        this._store.dispatch(new wizardActions.ResetWizardAction());                    // Reset wizard to default settings
        const type = 'Streaming'; //this._route.snapshot.params.type === 'streaming' ? 'Streaming' : 'Batch';
        const id = this._route.snapshot.params.id;

        if (id && id.length) {
            this.isEdit = true;
            this._store.dispatch(new wizardActions.ModifyWorkflowAction(id));
        } else {
            this._wizardService.workflowType = type;
            this._store.dispatch(new wizardActions.SetWorkflowTypeAction(type));
            this._store.dispatch(new wizardActions.GetMenuTemplatesAction());
        }

        this._workflowTypeSubscription = this._store.select(fromRoot.getWorkflowType).subscribe((workflowType: string) => {
            this._wizardService.workflowType = workflowType;
            this.workflowType = workflowType;
        });
    }

    ngOnInit(): void {
        this.creationMode$ = this._store.select(fromRoot.isCreationMode);               // show create node pointer icon
        this.editionConfigMode$ = this._store.select(fromRoot.getEditionConfigMode);    // show node/settings editor view
        this.showSettings$ = this._store.select(fromRoot.showSettings);
        this._saveSubscription = this._store.select(fromRoot.isSavedWorkflow).subscribe((isSaved: boolean) => {
            if (isSaved) {
                if (window.history.length > 2) {
                    this._location.back();
                } else {
                    this._router.navigate(['workflow-managing']);
                }
            }
        });
    }

    ngOnDestroy(): void {
        this._paramSubscription && this._paramSubscription.unsubscribe();
        this._saveSubscription && this._saveSubscription.unsubscribe();
        this._workflowTypeSubscription && this._workflowTypeSubscription.unsubscribe();
    }
}
