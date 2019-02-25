/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';

import { Parameter, Status } from '../../types/execution-detail';
import { State, executionDetailParametersState, executionDetailStatusesState, executionDetailState } from '../../reducers';
import * as executionDetailActions from '../../actions/execution-detail';

@Component({
  selector: 'sparta-execution-detail-table-container',
  template: `
    <sparta-execution-detail-drop-down-title [title]="statusesTitle">
      <sparta-execution-detail-table
        [isSearchable]="false"
        [fields]="runtimeFields"
        [values]="executionDetailStatusesState$ | async">
      </sparta-execution-detail-table>
    </sparta-execution-detail-drop-down-title>
    <sparta-execution-detail-drop-down-title [title]="parametersTitle" [IsVisibleContent]="true">
      <sparta-execution-detail-table
        [fields]="parametersFields"
        [values]="executionDetailParametersState$ | async">
      </sparta-execution-detail-table>
    </sparta-execution-detail-drop-down-title>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExecutionDetailTableContainer implements OnInit {

  public executionDetailParametersState$: Observable<Array<Parameter>>;
  public executionDetailStatusesState$: Observable<Array<Status>>;
  public parametersFields = [
    { id: 'name', label: 'Name' },
    { id: 'type', label: 'Type' },
    { id: 'lastModified', label: 'Last modified' }
  ];
  public runtimeFields = [
    { id: 'name', label: 'Status' },
    { id: 'statusInfo', label: 'Info' },
    { id: 'startTime', label: 'Start time' }
  ];
  public parametersTitle: String = 'Parameters';
  public statusesTitle: String = 'States';

  constructor(private _store: Store<State>) {}

  ngOnInit(): void {

    this.executionDetailParametersState$ = this._store.pipe(select(executionDetailParametersState));
    this.executionDetailStatusesState$ = this._store.pipe(select(executionDetailStatusesState));
  }

}
