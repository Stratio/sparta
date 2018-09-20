/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/distinctUntilChanged';

import {
   State
} from './reducers';

import * as executionsActions from './actions/executions';

import * as fromRoot from './reducers';

@Component({
   selector: 'sparta-executions',
   styleUrls: ['executions.styles.scss'],
   templateUrl: 'executions.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsComponent implements OnInit, OnDestroy {

   public executionsList$: Observable<any>;

   public executionsSummary$: Observable<any>;

   private _intervalHandler;
   constructor(private _store: Store<State>) { }

   ngOnInit() {
      this._store.dispatch(new executionsActions.ListExecutionsAction());
      this._intervalHandler = setInterval(() => this._store.dispatch(new executionsActions.ListExecutionsAction()), 3000);

      this.executionsList$ = this._store.select(fromRoot.getExecutionsList);
      this.executionsSummary$ = this._store.select(fromRoot.getExecutionsFilters);
   }

    ngOnDestroy(): void {
      clearInterval(this._intervalHandler);
    }
}
