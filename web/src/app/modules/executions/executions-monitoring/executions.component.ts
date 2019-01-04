/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { Store, select } from '@ngrx/store';

import {
   State
} from './reducers';

import * as executionsActions from './actions/executions';

import * as fromRoot from './reducers';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

@Component({
   selector: 'sparta-executions',
   styleUrls: ['executions.styles.scss'],
   templateUrl: 'executions.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsComponent implements OnInit, OnDestroy {

  public executionsList$: Observable<any>;
  public executionsSummary$: Observable<any>;
  public isLoading$: Observable<boolean>;
  public chartData$: Observable<any>;

  constructor(private _store: Store<State>, private _router: Router) { }

  ngOnInit() {
    this._store.dispatch(new executionsActions.ListExecutionsAction());
    this._store.dispatch(new executionsActions.SetGraphDataPeriodAction('DAY'));
    this.executionsList$ = this._store.pipe(select(fromRoot.getExecutionOrderedList));
    this.executionsSummary$ = this._store.pipe(select(fromRoot.getExecutionsFilters));
    this.isLoading$ = this._store.pipe(select(fromRoot.getIsLoading));
    this.chartData$ = this._store.pipe(select(fromRoot.getChartData));
  }

  goToRepository() {
    this._router.navigate(['repository']);
  }

  updatePeriod(period: string) {
    this._store.dispatch(new executionsActions.SetGraphDataPeriodAction(period));
  }

  ngOnDestroy(): void {
    this._store.dispatch(new executionsActions.CancelExecutionPollingAction());
  }
}
