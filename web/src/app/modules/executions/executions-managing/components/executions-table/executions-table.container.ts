/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { StModalService, Order } from '@stratio/egeo';

import { State } from './../../reducers';
import * as executionActions from '../../actions/executions';
import * as fromRoot from './../../reducers';
import { Observable } from 'rxjs/Observable';

@Component({
   selector: 'executions-managing-table-container',
   template: `
        <executions-managing-table [executionList]="executionList"
            [selectedExecutionsIds]="selectedExecutionsIds"
            [perPage]="perPage$ | async"
            [currentOrder]="currentOrder$ | async"
            [currentPage]="pageNumber$ | async"
            (onChangeOrder)="changeOrder($event)"
            (onChangePage)="changePage($event)"
            (selectExecution)="selectExecution($event)"
            (deselectExecution)="deselectExecution($event)"></executions-managing-table>
    `,
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsTableContainer implements OnInit, OnDestroy {

   @Input() selectedExecutionsIds: Array<string> = [];
   @Input() executionList: Array<any> = [];

   public perPage$: Observable<number>;
   public pageNumber$: Observable<number>;
   public currentOrder$: Observable<Order>;

   constructor(private _store: Store<State>, private _modalService: StModalService) { }

   ngOnInit(): void {
     this.pageNumber$ = this._store.select(fromRoot.getCurrentPage);
     this.perPage$ = this._store.select(fromRoot.getPerPageElements);
    }

    ngOnDestroy(): void {
      this._store.dispatch(new executionActions.ResetValuesAction());
    }

   selectExecution(execution: any) {
      this._store.dispatch(new executionActions.SelectExecutionAction(execution));
    }

   deselectExecution(execution: any) {
      this._store.dispatch(new executionActions.DeselectExecutionAction(execution));
   }

   changeOrder(event: Order) {
      this._store.dispatch(new executionActions.ChangeExecutionsOrderAction(event));
   }

   changePage($event) {
      this._store.dispatch(new executionActions.ChangePaginationAction($event));
   }



}
