/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, NgForm, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs/Subscription';

import * as workflowActions from './../../actions/workflow-list';
import * as fromRoot from './../../reducers';
import { ErrorMessagesService } from 'app/services';

@Component({
    selector: 'workflow-group-modal',
    templateUrl: './workflow-group-modal.template.html',
    styleUrls: ['./workflow-group-modal.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowGroupModal implements OnInit, OnDestroy {

    @Output() onCloseGroupModal = new EventEmitter<string>();

    public groupForm: FormGroup;
    public forceValidations = false;
    public name = '';

    private openModal$: Subscription;

    constructor(private _store: Store<fromRoot.State>, public errorsService: ErrorMessagesService, private _fb: FormBuilder) {
        _store.dispatch(new workflowActions.InitCreateGroupAction());
        this.groupForm = _fb.group({
            name: new FormControl('', [Validators.required])
        });
    }

    createGroup() {
        if (this.groupForm.valid) {
            this._store.dispatch(new workflowActions.CreateGroupAction(this.groupForm.controls.name.value));
        } else {
            this.forceValidations = true;
        }
    }

    ngOnInit() {
        this._store.dispatch(new workflowActions.ResetModalAction());
        this.openModal$ = this._store.select(fromRoot.getShowModal).subscribe((modalOpen) => {
            if (!modalOpen) {
                this.onCloseGroupModal.emit();
            }
        });
    }

    ngOnDestroy(): void {
        this.openModal$ && this.openModal$.unsubscribe();
    }

}

