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
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';

import { State } from './../../reducers';
import * as executionActions from '../../actions/executions';
import * as fromRoot from './../../reducers';

@Component({
   selector: 'executions-managing-header-container',
   template: `
        <executions-managing-header [selectedExecutions]="selectedExecutions"
            [isArchivedPage]="isArchivedPage"
            [showDetails]="showDetails"
            [fixSubHeaders]="fixSubHeaders"
            [statusFilter]="statusFilter$ | async"
            [showStopButton]="showStopButton$ | async"
            [showArchiveButton]="showArchiveButton$ | async"
            [showUnarchiveButton]="showUnarchiveButton$ | async"
            [typeFilter]="typeFilter$ | async"
            [emptyTable]="emptyTable"
            [timeIntervalFilter]="timeIntervalFilter$ | async"
            (downloadExecutions)="downloadExecutions()"
            (archiveExecutions)="archiveExecutions()"
            (unarchiveExecutions)="unarchiveExecutions()"
            (showExecutionInfo)="showExecutionInfo.emit()"
            (onStopExecution)="stopExecution()"
            (onDeleteExecution)="deleteExecution($event)"
            (onReRunExecution)="reRunExecution($event)"
            (onChangeStatusFilter)="changeStatusFilter($event)"
            (onChangeTypeFilter)="changeTypeFilter($event)"
            (onChangeTimeIntervalFilter)="changeTimeIntervalFilter($event)"
            (onSearch)="searchExecutions($event)"></executions-managing-header>
    `,
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsHeaderContainer implements OnInit {

   @Input() selectedExecutions: Array<any>;
   @Input() showDetails: boolean;
   @Input() emptyTable: boolean;
   @Input() isArchivedPage: boolean;
   @Input() fixSubHeaders: boolean;

   @Output() showExecutionInfo = new EventEmitter<void>();

   public statusFilter$: Observable<string>;
   public typeFilter$: Observable<string>;
   public timeIntervalFilter$: Observable<number>;
   public showStopButton$: Observable<boolean>;
   public showArchiveButton$: Observable<boolean>;
   public showUnarchiveButton$: Observable<boolean>;

   constructor(private _store: Store<State>) { }

   ngOnInit(): void {
      this.statusFilter$ = this._store.pipe(select(fromRoot.getStatusFilter));
      this.typeFilter$ = this._store.pipe(select(fromRoot.getTypeFilter));
      this.timeIntervalFilter$ = this._store.pipe(select(fromRoot.getTimeIntervalFilter));
      if (this.isArchivedPage) {
         this.showUnarchiveButton$ = this._store.pipe(select(fromRoot.showUnarchiveButton));
      } else {
         this.showStopButton$ = this._store.pipe(select(fromRoot.showStopButton));
         this.showArchiveButton$ = this._store.pipe(select(fromRoot.showArchiveButton));
      }

   }

   downloadExecutions(): void { }

   archiveExecutions() {
      this._store.dispatch(new executionActions.ArchiveExecutionsAction());
   }

   unarchiveExecutions() {
      this._store.dispatch(new executionActions.UnarchiveExecutionsAction());
   }

   searchExecutions(text: string) {
      this._store.dispatch(new executionActions.SearchExecutionAction(text));
   }

   changeStatusFilter(status: string) {
      this._store.dispatch(new executionActions.SelectStatusFilterAction(status));
   }

   changeTypeFilter(workflowType: string) {
      this._store.dispatch(new executionActions.SelectTypeFilterAction(workflowType));
   }

   changeTimeIntervalFilter(time: number) {
      this._store.dispatch(new executionActions.SelectTimeIntervalFilterAction(time));
   }

   stopExecution() {
      this._store.dispatch(new executionActions.StopExecutionAction());
   }

   reRunExecution(executionId: string) {
     this._store.dispatch(new executionActions.ReRunExecutionAction(executionId));
   }

   deleteExecution(executionId: string) {
      this._store.dispatch(new executionActions.DeleteExecutionAction(executionId));
   }
}
