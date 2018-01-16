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
    Output
} from '@angular/core';
import { Store } from '@ngrx/store';

import * as workflowActions from './../../actions/workflow-list';
import { State } from './../../reducers';

@Component({
    selector: 'workflows-header-container',
    template: `
        <workflows-header [selectedWorkflows]="selectedWorkflows"
            [showDetails]="showDetails" 
            (downloadWorkflows)="downloadWorkflows()" 
            (showWorkflowInfo)="showWorkflowInfo.emit()"
            (onDeleteWorkflows)="deleteWorkflows($event)"></workflows-header>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsHeaderContainer {

    @Input() selectedWorkflows: Array<any>;
    @Input() showDetails: boolean;

    @Output() showWorkflowInfo = new EventEmitter<void>();

    constructor(private _store: Store<State>) { }

    downloadWorkflows(): void {
        this._store.dispatch(new workflowActions.DownloadWorkflowsAction(this.selectedWorkflows));
    }

    selectGroup(name: string) {
        this._store.dispatch(new workflowActions.ChangeGroupLevelAction(name));
    }

    deleteWorkflows(workflows: Array<any>): void {
        this._store.dispatch(new workflowActions.DeleteWorkflowAction(workflows));
    }

}
