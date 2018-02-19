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
import { FOLDER_SEPARATOR, DEFAULT_FOLDER } from './../../workflow.constants';

@Component({
    selector: 'move-group-modal',
    templateUrl: './move-group.template.html',
    styleUrls: ['./move-group.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MoveGroupModal implements OnInit, OnDestroy {

    @Input() workflow: any;
    @Input() currentGroup: string;
    @Output() onCloseMoveGroup = new EventEmitter<string>();

    @ViewChild('renameForm') public renameForm: NgForm;

    public forceValidations = false;
    public groups: any = [];

    public parentGroup = '';
    public workflowGroup = '';

    private group: any;
    public selectedFolder = '';
    private openModal: Subscription;

    constructor(private _store: Store<fromRoot.State>) { }

    ngOnInit() {
        this._store.dispatch(new workflowActions.ResetModalAction());
        this._store.select(fromRoot.getAllGroups).take(1).subscribe((groups: any) => {
            this.groups = groups;
        });

        if (this.workflow) {
            this._store.select(fromRoot.getCurrentGroupLevel).take(1).subscribe((current: any) => {
                this.parentGroup = current.group.name;
                this.group = current.group;
            });
        } else {
            const split = this.currentGroup.split(FOLDER_SEPARATOR);
            this.parentGroup = split.slice(0, split.length - 1).join(FOLDER_SEPARATOR);
        }
        this.openModal = this._store.select(fromRoot.getShowModal).subscribe((modalOpen) => {
            if (!modalOpen) {
                this.onCloseMoveGroup.emit();
            }
        });
    }

    selectFolder(folder: string) {
        this.selectedFolder = folder;
    }

    updateFolder() {
        if (this.workflow) {
            this._store.dispatch(new workflowActions.MoveWorkflowAction({
                groupTarget: this.selectedFolder, // get target id
                groupSourceId: this.group.id,
                workflowName: this.workflow
            }));
        } else {
            const split = this.currentGroup.split(FOLDER_SEPARATOR);
            this._store.dispatch(new workflowActions.RenameGroupAction({
                oldName: this.currentGroup,
                newName: this.selectedFolder + FOLDER_SEPARATOR + split[split.length - 1]
            }));
        }

    }

    ngOnDestroy(): void {
        this.openModal && this.openModal.unsubscribe();
    }

}

