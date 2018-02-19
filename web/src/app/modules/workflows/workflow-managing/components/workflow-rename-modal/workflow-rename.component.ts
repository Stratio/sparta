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
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
    ViewChild
} from '@angular/core';
import { NgForm } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs/Rx';

import * as workflowActions from './../../actions/workflow-list';
import * as fromRoot from './../../reducers';
import { ErrorMessagesService } from 'app/services';
import { FOLDER_SEPARATOR } from './../../workflow.constants';

@Component({
    selector: 'workflow-rename-modal',
    templateUrl: './workflow-rename.template.html',
    styleUrls: ['./workflow-rename.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowRenameModal implements OnInit, OnDestroy {

    @Input() entityType = 'Group';
    @Input() entityName = '';
    @Output() onCloseRenameModal = new EventEmitter<string>();

    @ViewChild('renameForm') public renameForm: NgForm;

    public forceValidations = false;
    public name = '';
    public currentGroup = '';

    private openModal: Subscription;

    constructor(private _store: Store<fromRoot.State>, public errorsService: ErrorMessagesService) {
        _store.dispatch(new workflowActions.InitCreateGroupAction());
    }

    updateEntity() {
        if (this.renameForm.valid) {
            if (this.entityType === 'Group') {
                this._store.dispatch(new workflowActions.RenameGroupAction({
                    oldName: this.entityName,
                    newName: this.currentGroup + FOLDER_SEPARATOR + this.name
                }));
            } else {
                this._store.dispatch(new workflowActions.RenameWorkflowAction({
                    oldName: this.entityName,
                    newName: this.name
                }));
            }
        } else {
            this.forceValidations = true;
        }
    }

    ngOnInit() {
        if (this.entityType === 'Group') {
            const splittedName = this.entityName.split(FOLDER_SEPARATOR);
            this.currentGroup = splittedName.slice(0, splittedName.length - 1).join(FOLDER_SEPARATOR);
            this.name = splittedName[splittedName.length - 1];
        } else {
            this.name = this.entityName;
        }
        this._store.dispatch(new workflowActions.ResetModalAction());
        this.openModal = this._store.select(fromRoot.getShowModal).subscribe((modalOpen) => {
            if (!modalOpen) {
                this.onCloseRenameModal.emit();
            }
        });
    }

    ngOnDestroy(): void {
        this.openModal && this.openModal.unsubscribe();
    }

}

