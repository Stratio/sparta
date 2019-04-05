/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, OnInit, OnDestroy } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { State } from './reducers';

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
         [selectedExecutions]="selectedExecutions$ | async"
         (selectExecution)="selectExecution($event)"
         ></scheduled-list>
    `,
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ScheduledContainer implements OnInit, OnDestroy {


  public typeFilter$: Observable<StDropDownMenuItem>;
  public timeIntervalFilter$: Observable<StDropDownMenuItem>;
  public scheduledExecutions$: Observable<Array<ScheduledExecution>>;
  public selectedExecutions$: Observable<Array<string>>;

  constructor(private _store: Store<State>) { }

  ngOnInit(): void {
    this._store.dispatch(new scheduledActions.ListScheduledExecutionsAction());
    this.timeIntervalFilter$ = this._store.pipe(select(fromRoot.getSchedulesTimeIntervalFilterValue));
    this.scheduledExecutions$ = this._store.pipe(select(fromRoot.getScheduledExecutions));
    this.typeFilter$ = this._store.pipe(select(fromRoot.getSchedulesWorkflowTypesFilterValue));
    this.selectedExecutions$ = this._store.pipe(select(fromRoot.getSelectedExecutions));
  }

  ngOnDestroy(): void {
    this._store.dispatch(new scheduledActions.CancelListScheduledExecutionsAction());
  }

  selectExecution(executionId: string) {
    this._store.dispatch(new scheduledActions.ToggleExecutionSelectionAction(executionId));
  }
}
