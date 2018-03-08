/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
