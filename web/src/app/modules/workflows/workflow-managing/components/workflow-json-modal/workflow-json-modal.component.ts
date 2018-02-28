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

import { Component, OnInit, Output, EventEmitter, ViewChild, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Store } from '@ngrx/store';

import * as workflowActions from './../../actions/workflow-list';
import * as fromRoot from './../../reducers';
import { Subscription, Observable } from 'rxjs/Rx';

@Component({
    selector: 'workflow-json-modal',
    templateUrl: './workflow-json-modal.template.html',
    styleUrls: ['./workflow-json-modal.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowJsonModal implements OnInit {

    @Output() onCloseJsonModal = new EventEmitter<string>();

    @ViewChild('jsonWorkflowForm') public jsonWorkflowForm: NgForm;

    public workflowValidationError = false;
    public forceValidations = false;
    public serverErrorsSubscription: Subscription;
    public serverErrors = '';
    private openModal: Subscription;

    public model: any = {
        name: '',
        description: '',
        json: ''
    };

    constructor(private store: Store<fromRoot.State>, private _cd: ChangeDetectorRef) { }

    onChangedFile($event: string): void {
        try {
            const parsedJson = JSON.parse($event);
            this.model.name = parsedJson.name;
            this.model.description = parsedJson.description;
            this.model.json = JSON.stringify(JSON.parse($event), undefined, 3);
        } catch (error) {
            console.log('Parse error. Expected JSON file.');
        }
    }

    onSubmitWorkflow(): void {
        this.workflowValidationError = false;
        this.serverErrors = '';
        if (this.jsonWorkflowForm.valid) {
            try {
                const parsedJson = JSON.parse(this.model.json);
                const name = this.model.name;
                const description = this.model.description;

                /* override json value */
                if (name.length) {
                    parsedJson.name = name;
                }

                if (description.length) {
                    parsedJson.description = description;
                }

                this.store.dispatch(new workflowActions.SaveJsonWorkflowAction(parsedJson));
            } catch (error) {
                this.workflowValidationError = true;
            }
        } else {
            this.forceValidations = true;
        }
    }

    ngOnInit() {

        this.serverErrorsSubscription = this.store.select(fromRoot.getModalError).subscribe((error: any) => {
            if (error.error && error.error.length) {
                try {
                    const parsed = JSON.parse(error.error);
                    this.serverErrors = parsed.exception;

                } catch (e) {
                    this.serverErrors = error.error;
                }
            } else {
                this.serverErrors = '';
            }
            this._cd.markForCheck();
        });

        this.store.dispatch(new workflowActions.ResetModalAction());
        this.openModal = this.store.select(fromRoot.getShowModal).subscribe((modalOpen) => {
            if (!modalOpen) {
                this.onCloseJsonModal.emit();
            }
        });
    }

    ngOnDestroy(): void {
        this.openModal && this.openModal.unsubscribe();
        this.serverErrorsSubscription && this.serverErrorsSubscription.unsubscribe();
    }

}

