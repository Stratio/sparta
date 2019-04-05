/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, OnInit, OnDestroy } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { State } from './reducers';

import * as scheduledFiltersActions from './actions/scheduled-filters';
import * as scheduledActions from './actions/scheduled';
import * as fromRoot from './reducers';
import { Observable } from 'rxjs';
import { StDropDownMenuItem } from '@stratio/egeo';
import { ScheduledExecution } from './models/scheduled-executions';


@Component({
  selector: 'scheduled-container',
  template: `
        <scheduled-list
         [typeFilter]="typeFilter$ | async"
         [timeIntervalFilter]="timeIntervalFilter$ | async"
         [scheduledExecutions]="scheduledExecutions$ | async"
         [isEmptyScheduledExecutions]="isEmptyScheduledExecutions$ | async"
         [selectedExecutions]="selectedExecutions$ | async"
         [searchQuery]="searchQuery$ | async"
         (selectExecution)="selectExecution($event)"
         (deleteExecution)="deleteExecution($event)"
         (startExecution)="startExecution($event)"
         (stopExecution)="stopExecution($event)"
         (onChangeTypeFilter)="onChangeTypeFilter($event)"
         (onSearch)="onSearch($event)"
         ></scheduled-list>
    `,
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ScheduledContainer implements OnInit, OnDestroy {


  public typeFilter$: Observable<StDropDownMenuItem>;
  public timeIntervalFilter$: Observable<StDropDownMenuItem>;
  public scheduledExecutions$: Observable<Array<ScheduledExecution>>;
  public selectedExecutions$: Observable<Array<string>>;
  public isEmptyScheduledExecutions$: Observable<boolean>;
  public searchQuery$: Observable<string>;

  constructor(private _store: Store<State>) { }

  ngOnInit(): void {
    this._store.dispatch(new scheduledActions.ListScheduledExecutionsAction());
    this.isEmptyScheduledExecutions$ = this._store.pipe(select(fromRoot.isEmptyScheduledExecutions));
    this.timeIntervalFilter$ = this._store.pipe(select(fromRoot.getSchedulesTimeIntervalFilterValue));
    this.scheduledExecutions$ = this._store.pipe(select(fromRoot.getScheduledSearchedExecutions));
    this.typeFilter$ = this._store.pipe(select(fromRoot.getSchedulesWorkflowTypesFilterValue));
    this.selectedExecutions$ = this._store.pipe(select(fromRoot.getSelectedExecutions));
    this.searchQuery$ = this._store.pipe(select(fromRoot.getSearchQuery));
  }

  ngOnDestroy(): void {
    this._store.dispatch(new scheduledActions.CancelListScheduledExecutionsAction());
  }

  selectExecution(executionId: string) {
    this._store.dispatch(new scheduledActions.ToggleExecutionSelectionAction(executionId));
  }

  startExecution(execution: ScheduledExecution) {
    this._store.dispatch(new scheduledActions.StartScheduledExecution(execution));
  } 

  stopExecution(execution: ScheduledExecution) {
    this._store.dispatch(new scheduledActions.StopScheduledExecution(execution));
  }

  deleteExecution(executionId: string) {
    this._store.dispatch(new scheduledActions.DeleteScheduledExecution(executionId));
  }

  onSearch(searchQuery: string) {
    this._store.dispatch(new scheduledFiltersActions.SearchScheduledExecutions(searchQuery));
  }

  onChangeTypeFilter(event: any) {
    this._store.dispatch(new scheduledFiltersActions.ChangeTypeFilter(event));
  }
}
