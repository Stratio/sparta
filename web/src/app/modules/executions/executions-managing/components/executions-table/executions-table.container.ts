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
import { Observable } from 'rxjs';

@Component({
   selector: 'executions-managing-table-container',
   template: `
        <executions-managing-table [executionList]="executionList"
            [selectedExecutionsIds]="selectedExecutionsIds"
            [perPage]="perPage$ | async"
            [areAllSelected]="allSelectedStates$ | async"
            [currentOrder]="currentOrder$ | async"
            [currentPage]="pageNumber$ | async"
            [total]="total$ | async"
            (onChangeOrder)="changeOrder($event)"
            (onChangePage)="changePage($event)"
            (selectExecution)="selectExecution($event)"
            (deselectExecution)="deselectExecution($event)"
            (allExecutionsToggled)="toggleAllExecutions($event)"></executions-managing-table>
    `,
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsTableContainer implements OnInit {

   @Input() selectedExecutionsIds: Array<string> = [];
   @Input() executionList: Array<any> = [];

   public perPage$: Observable<number>;
   public pageNumber$: Observable<number>;
   public total$: Observable<number>;
   public currentOrder$: Observable<Order>;
   public allSelectedStates$: Observable<Boolean>;

   constructor(private _store: Store<State>, private _modalService: StModalService) { }

   ngOnInit(): void {
     this.pageNumber$ = this._store.select(fromRoot.getCurrentPage);
     this.perPage$ = this._store.select(fromRoot.getPerPageElements);
     this.total$ = this._store.select(fromRoot.getTotalElements);
     this.allSelectedStates$ = this._store.select(fromRoot.getAllSelectedStates);
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

   toggleAllExecutions(isChecked: boolean) {
     if (isChecked) {
      this._store.dispatch(new executionActions.ChangeExecutionsSelectAllExecutions());
     } else {
      this._store.dispatch(new executionActions.ChangeExecutionsDeselectAllExecutions());
     }
   }

}
