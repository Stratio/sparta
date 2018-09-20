/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { Store } from '@ngrx/store';
import { Router } from '@angular/router';

import * as fromRoot from './../../reducers';
import { State } from './../../reducers';
import { SelectStatusFilterAction } from '@app/executions/executions-managing/actions/executions';

@Component({
   selector: 'executions-header',
   styleUrls: ['executions-header.component.scss'],
   templateUrl: 'executions-header.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsHeaderComponent {

   @Input() filters: Array<any> = [];
  
   constructor(private _store: Store<State>, private _router: Router) { }

   showExecutions(status: string) {
    this._store.dispatch(new SelectStatusFilterAction(status.charAt(0).toUpperCase() + status.slice(1)));
    this._router.navigate(['executions']);
   }
}
