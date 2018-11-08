/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, Output, EventEmitter, ViewChild, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { Subscription } from 'rxjs';

import * as workflowActions from './../../actions/workflow-list';
import * as fromRoot from './../../reducers';

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
    public isSaving$: Observable<boolean>;

    private openModal: Subscription;

    public model: any = {
        name: '',
        description: '',
        json: ' '
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
                delete this.model.id;
                this.store.dispatch(new workflowActions.SaveJsonWorkflowAction(parsedJson));
            } catch (error) {
                this.workflowValidationError = true;
            }
        } else {
            this.forceValidations = true;
        }
    }

    ngOnInit() {
       this.isSaving$ = this.store.select(fromRoot.getSavingState);
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

