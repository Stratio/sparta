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

import { State } from './../../reducers';
import * as executionActions from '../../actions/executions';

@Component({
    selector: 'executions-managing-header-container',
    template: `
        <executions-managing-header [selectedExecutions]="selectedExecutions"
            [showDetails]="showDetails"
            (downloadExecutions)="downloadExecutions()"
            (showExecutionInfo)="showExecutionInfo.emit()"
            (onRunExecutions)="onRunExecutions($event)"
            (onStopExecution)="stopExecution()"
            (onSearch)="searchExecutions($event)"></executions-managing-header>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsHeaderContainer implements OnInit {

   @Input() selectedExecutions: Array<any>;
   @Input() showDetails: boolean;

   @Output() showExecutionInfo = new EventEmitter<void>();

   constructor(private _store: Store<State>) { }

   ngOnInit(): void { }

   downloadExecutions(): void { }

   searchExecutions(text: string) { }

   onRunExecutions(execution: any) { }

   stopExecution() {
      this._store.dispatch(new executionActions.StopExecutionAction());
   }
}
