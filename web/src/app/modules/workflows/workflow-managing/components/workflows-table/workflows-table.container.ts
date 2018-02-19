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
    Output,
    OnInit
} from '@angular/core';
import { Store } from '@ngrx/store';
import { StModalService } from '@stratio/egeo';

import * as workflowActions from './../../actions/workflow-list';
import { State, getWorkflowVersions } from './../../reducers';
import { Observable } from 'rxjs/Observable';

@Component({
    selector: 'workflows-manage-table-container',
    template: `
        <workflows-manage-table [workflowList]="workflowList"
            [workflowVersions]="workflowVersions"
            [selectedGroupsList]="selectedGroupsList"
            [selectedWorkflows]="selectedWorkflows"
            [selectedVersions]="selectedVersions"
            [groupList]="groupList"
            (onChangeOrder)="changeOrder($event)"
            (onChangeOrderVersions)="changeOrderVersions($event)"
            (changeFolder)="changeFolder($event)"
            (openWorkflow)="showWorkflowVersions($event)"
            (selectWorkflow)="selectWorkflow($event)"
            (selectGroup)="selectGroup($event)"
            (selectVersion)="selectVersion($event)"></workflows-manage-table>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsManagingTableContainer implements OnInit {

    @Input() selectedWorkflows: Array<string> = [];
    @Input() selectedGroupsList: Array<string> = [];
    @Input() workflowList: Array<any> = [];
    @Input() groupList: Array<any> = [];
    @Input() selectedVersions: Array<string> = [];
    @Input() workflowVersions: Array<any> = [];

    @Output() showWorkflowInfo = new EventEmitter<void>();

    public workflowVersions$: Observable<Array<any>>;

    ngOnInit(): void {
        this.workflowVersions$ = this._store.select(getWorkflowVersions);
    }


    changeOrder(event: any) {
        this._store.dispatch(new workflowActions.ChangeOrderAction(event));
    }

    changeOrderVersions(event: any) {
        this._store.dispatch(new workflowActions.ChangeVersionsOrderAction(event));
    }

    selectWorkflow(name: string) {
        this._store.dispatch(new workflowActions.SelectWorkflowAction(name));
    }

    selectGroup(name: string) {
        this._store.dispatch(new workflowActions.SelectGroupAction(name));
    }

    changeFolder(event: any) {
        this._store.dispatch(new workflowActions.ChangeGroupLevelAction(event));
    }

    selectVersion(id: string) {
        this._store.dispatch(new workflowActions.SelectVersionAction(id));
    }

    showWorkflowVersions(workflow: any) {
        this._store.dispatch(new workflowActions.ShowWorkflowVersionsAction({
            name: workflow.name,
            group: workflow.group
        }));
    }

    constructor(private _store: Store<State>, private _modalService: StModalService) { }


}
