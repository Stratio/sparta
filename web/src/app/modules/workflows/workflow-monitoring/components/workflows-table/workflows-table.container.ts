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
import { StModalService, Order } from '@stratio/egeo';
import { Observable } from 'rxjs/Observable';

import * as workflowActions from './../../actions/workflows';
import * as filtersActions from './../../actions/filters';

import { State, getPaginationNumber, getTableOrder } from './../../reducers';
import { MonitoringWorkflow } from '../../models/workflow';

@Component({
   selector: 'workflows-table-container',
   template: `
        <workflows-table [workflowList]="workflowList"
            [selectedWorkflowsIds]="selectedWorkflowsIds"
            [paginationOptions]="paginationOptions$ | async"
            [currentOrder]="currentOrder$ | async"
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
   @Input() workflowList: Array<MonitoringWorkflow> = [];

   @Output() showWorkflowInfo = new EventEmitter<void>();

   public paginationOptions$: Observable<any>;
   public currentOrder$: Observable<Order>;

   ngOnInit(): void {
      this.paginationOptions$ = this._store.select(getPaginationNumber);
      this.currentOrder$ = this._store.select(getTableOrder);
   }

   changeOrder(event: Order) {
      this._store.dispatch(new filtersActions.ChangeOrderAction(event));
   }

   selectWorkflow(workflow: MonitoringWorkflow) {
      this._store.dispatch(new workflowActions.SelectWorkflowAction(workflow));
   }

   deselectWorkflow(workflow: MonitoringWorkflow) {
      this._store.dispatch(new workflowActions.DeselectWorkflowAction(workflow));
   }

   changePage() {
      this._store.dispatch(new workflowActions.ResetSelectionAction());
   }

   changeCurrentPage(pageOptions: any) {
      this._store.dispatch(new filtersActions.SetPaginationNumber(pageOptions));
   }

   constructor(private _store: Store<State>, private _modalService: StModalService) { }


}
