/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
   ChangeDetectionStrategy,
   ChangeDetectorRef,
   Component,
   OnInit,
   ViewChild,
   ViewContainerRef
} from '@angular/core';
import { Store } from '@ngrx/store';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/distinctUntilChanged';
import { Subscription } from 'rxjs/Subscription';
import { StModalService } from '@stratio/egeo';

import {
   State
} from './reducers';

import * as executionsActions from './actions/executions';

import * as fromRoot from './reducers';

@Component({
   selector: 'sparta-managing-executions',
   styleUrls: ['executions.styles.scss'],
   templateUrl: 'executions.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsManagingComponent implements OnInit {

   @ViewChild('newExecutionsModalMonitoring', { read: ViewContainerRef }) target: any;

   public showDetails = false;

   public selectedExecutions: Array<any> = [];
   public selectedExecutionsIds: string[] = [];
   public executionInfo: any;
   public showInitialMode: Observable<boolean>;
   public executionsList$: Observable<any>;

   private _selectedExecutions: Subscription;


   private timer;

   constructor(private _store: Store<State>,
      private _route: Router,
      private _cd: ChangeDetectorRef,
      private _modalService: StModalService) { }

   ngOnInit() {
      this._store.dispatch(new executionsActions.ListExecutionsAction());
      this.executionsList$ = this._store.select(fromRoot.getExecutionsList);

      this._selectedExecutions = this._store.select(fromRoot.getSelectedExecutions).subscribe((selectedIds: any) => {
         this.selectedExecutionsIds = selectedIds;
         this._cd.markForCheck();
      });
   }

   onShowExecutionInfo() {
      this.showDetails = !this.showDetails;
   }

   showWorkflowExecutionInfo(ev) { }

   showConsole(ev) { }
}
