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
    selector: 'workflow-duplicate-modal',
    templateUrl: './duplicate-workflow.component.html',
    styleUrls: ['./duplicate-workflow.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DuplicateWorkflowComponent implements OnInit, OnDestroy {

    @Input() version: any;
    @Output() onCloseDuplicateModal = new EventEmitter<string>();

    @ViewChild('renameForm') public renameForm: NgForm;

    public forceValidations = false;
    public name = '';
    public currentGroup = '';
    public parentGroup = '';
    public groups: any = [];
    public selectedFolder = '';

    private openModal: Subscription;

    constructor(private _store: Store<fromRoot.State>, public errorsService: ErrorMessagesService) {
        _store.dispatch(new workflowActions.InitCreateGroupAction());
    }


    ngOnInit() {
        this._store.dispatch(new workflowActions.ResetModalAction());
        this.openModal = this._store.select(fromRoot.getShowModal).subscribe((modalOpen) => {
            if (!modalOpen) {
                this.onCloseDuplicateModal.emit();
            }
        });
        this.currentGroup = this.version.group;
        this.name =  this.version.name;
        this._store.select(fromRoot.getAllGroups).take(1).subscribe((groups: any) => {
            this.parentGroup = this.currentGroup;
            this.groups = groups;
        });
    }

    selectFolder(folder: string) {
        this.selectedFolder = folder;
    }

    duplicate() {
        this._store.dispatch(new workflowActions.DuplicateWorkflowAction({
            id: this.version.id,
            group: this.selectedFolder,
            tag: this.version.tag
        }));
    }

    ngOnDestroy(): void {
        this.openModal && this.openModal.unsubscribe();
    }

}

