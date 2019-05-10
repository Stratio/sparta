/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';

import {State, executionDetailInfoState, executionDetailShowedState, getLastExecutionError} from './../../reducers';

import {ExecutionDetailInfo} from '@app/executions/execution-detail/models/execution-detail-info';
import {Info, ShowedActions} from '@app/executions/execution-detail/types/execution-detail';
import * as ExecutionDetailInfoActions from '@app/executions/execution-detail/actions/execution-detail';

@Component({
  selector: 'workflow-execution-detail-info-container',
  template: `
    <workflow-execution-detail-info
      [executionDetailInfo]="executionDetailInfo$ | async"
      [showedActions]="showedActions$ | async"
      [lastError]="lastError$ | async"
      (onStopExecution)="stopExecution($event)"
      (onRerunExecution)="rerunExecution($event)"
      (showErrorDetails)="showErrorDetails()"
    ></workflow-execution-detail-info>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DetailInfoContainer implements OnInit {

  public executionDetailInfo$: Observable<Info>;
  public showedActions$: Observable<ShowedActions>;
  public lastError$: Observable<any>;

  constructor(private _store: Store<State>) {}

  ngOnInit(): void {
    this.executionDetailInfo$ = this._store.pipe(select(executionDetailInfoState));
    this.showedActions$ = this._store.pipe(select(executionDetailShowedState));
    this.lastError$ = this._store.pipe(select(getLastExecutionError));
  }

  stopExecution(executionId) {
    this._store.dispatch(new ExecutionDetailInfoActions.StopExecutionAction(executionId));
  }

  rerunExecution(executionId) {
    this._store.dispatch(new ExecutionDetailInfoActions.RerunExecutionAction(executionId));
  }

  showErrorDetails() {
    this._store.dispatch(new ExecutionDetailInfoActions.ShowConsoleAction());
  }

}
