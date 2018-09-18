/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { StModalService } from '@stratio/egeo';

import { State } from './../../reducers';
import * as executionActions from '../../actions/executions';

@Component({
   selector: 'executions-managing-table-container',
   template: `
        <executions-managing-table [executionList]="executionList"
            [selectedExecutionsIds]="selectedExecutionsIds"
            (selectExecution)="selectExecution($event)"
            (deselectExecution)="deselectExecution($event)"></executions-managing-table>
    `,
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsTableContainer implements OnInit {

   @Input() selectedExecutionsIds: Array<string> = [];
   @Input() executionList: Array<any> = [];

   ngOnInit(): void { }


   selectExecution(execution: any) {
      this._store.dispatch(new executionActions.SelectExecutionAction(execution));
    }

   deselectExecution(execution: any) {
      this._store.dispatch(new executionActions.DeselectExecutionAction(execution));
   }

   changePage() { }


   constructor(private _store: Store<State>, private _modalService: StModalService) { }

}
