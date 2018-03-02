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
import { Observable } from 'rxjs/Rx';

import * as workflowActions from './../../actions/workflow-list';
import { State, getPaginationNumber } from './../../reducers';

@Component({
    selector: 'workflows-table-container',
    template: `
        <workflows-table [workflowList]="workflowList"
            [selectedWorkflowsIds]="selectedWorkflowsIds"
            [currentPage]="currentPage$ | async"
            (onChangeOrder)="changeOrder($event)"
            (onChangePage)="changePage()"
            (changeCurrentPage)="changeCurrentPage($event)"
            (selectWorkflow)="selectWorkflow($event)"
            (deselectWorkflow)="deselectWorkflow($event)"></workflows-table>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsTableContainer implements OnInit {

    @Input() selectedWorkflowsIds: Array<string> = [];
    @Input() workflowList: Array<any> = [];

    @Output() showWorkflowInfo = new EventEmitter<void>();

    public currentPage$: Observable<number>;

    ngOnInit(): void {
        this.currentPage$ = this._store.select(getPaginationNumber);
    }

    changeOrder(event: any) {
        this._store.dispatch(new workflowActions.ChangeOrderAction(event));
    }

    selectWorkflow(event: any) {
        this._store.dispatch(new workflowActions.SelectWorkflowAction(event));
    }

    deselectWorkflow(event: any) {
        this._store.dispatch(new workflowActions.DeselectWorkflowAction(event));
    }

    changePage() {
        this._store.dispatch(new workflowActions.ResetSelectionAction());
    }

    changeCurrentPage(page: number) {
        this._store.dispatch(new workflowActions.SetPaginationNumber(page));
    }

    constructor(private _store: Store<State>, private _modalService: StModalService) { }


}
