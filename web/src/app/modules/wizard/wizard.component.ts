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

import * as fromRoot from 'reducers';
import * as wizardActions from 'actions/wizard';


@Component({
    selector: 'wizard',
    styleUrls: ['wizard.styles.scss'],
    templateUrl: 'wizard.template.html'
})

export class WizardComponent implements OnInit, OnDestroy {

    public creationMode$: Observable<any>;
    public editionConfigMode$: Observable<any>;
    private _paramSubscription: Subscription;
    private _saveSubscription: Subscription;

    constructor(
        private _cd: ChangeDetectorRef,
        private _store: Store<fromRoot.State>,
        private _translate: TranslateService,
        private _router: Router,
        private _route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this._store.dispatch(new wizardActions.ResetWizardAction());
        this.creationMode$ = this._store.select(fromRoot.isCreationMode);
        this.editionConfigMode$ = this._store.select(fromRoot.getEditionConfigMode);
        this._paramSubscription = this._route.params.subscribe(params => {
            if (params && params.id) {
                this._store.dispatch(new wizardActions.ModifyWorkflowAction(params.id));
            }
        });

        this._saveSubscription = this._store.select(fromRoot.isSavedWorkflow).subscribe((isSaved: boolean) => {
            if (isSaved) {
                this._router.navigate(['']);
            }
        });
    }

    ngOnDestroy(): void {
        this._paramSubscription && this._paramSubscription.unsubscribe();
        this._saveSubscription && this._saveSubscription.unsubscribe();
    }
}
