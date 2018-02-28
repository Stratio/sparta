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

import * as workflowActions from './../../actions/workflow-list';
import { State, getMonitoringStatus, getSelectedFilter, getWorkflowSearchQuery } from './../../reducers';
import { Observable } from 'rxjs/Observable';

@Component({
    selector: 'workflows-header-container',
    template: `
        <workflows-header [selectedWorkflows]="selectedWorkflows"
            [monitoringStatus]="monitoringStatus$ | async"
            [showDetails]="showDetails"
            [searchQuery]="searchQuery$ | async"
            [workflowListLength]="workflowListLength"
            [selectedFilter]="selectedFilter$ | async"
            (downloadWorkflows)="downloadWorkflows()" 
            (showWorkflowInfo)="showWorkflowInfo.emit()"
            (onSelectFilter)="selectFilter($event)"
            (onSearch)="searchWorkflows($event)"></workflows-header>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsHeaderContainer implements OnInit {

    @Input() selectedWorkflows: Array<any>;
    @Input() showDetails: boolean;
    @Input() workflowListLength: number;

    @Output() showWorkflowInfo = new EventEmitter<void>();

    public monitoringStatus$: Observable<any>;
    public selectedFilter$: Observable<string>;
    public searchQuery$: Observable<string>;

    ngOnInit(): void {
        this.searchQuery$ = this._store.select(getWorkflowSearchQuery);
        this.monitoringStatus$ = this._store.select(getMonitoringStatus);
        this.selectedFilter$ = this._store.select(getSelectedFilter);
    }

    constructor(private _store: Store<State>) { }

    downloadWorkflows(): void {
        this._store.dispatch(new workflowActions.DownloadWorkflowsAction(this.selectedWorkflows));
    }

    selectFilter(filter: string): void {
        this._store.dispatch(new workflowActions.ResetSelectionAction());
        this._store.dispatch(new workflowActions.ChangeFilterAction(filter));
    }

    searchWorkflows(text: string) {
        this._store.dispatch(new workflowActions.ResetSelectionAction());
        this._store.dispatch(new workflowActions.SearchWorkflowsAction(text));
    }

}
