/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, ChangeDetectionStrategy, Output, EventEmitter } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';

import { Parameter, Status } from '../../types/execution-detail';
import { QualityRule, globalActionMap } from '@app/executions/models';
import { State, executionDetailParametersState, executionDetailStatusesState, qualityRulesState, executionDetailfilterParametersState } from '../../reducers';
import { FilterParametersAction } from '../../actions/execution-detail';
import { map } from 'rxjs/operators';

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
    <sparta-execution-detail-drop-down-title [title]="parametersTitle">
      <sparta-execution-detail-table
        [fields]="parametersFields"
        [values]="executionDetailParametersState$ | async"
        [filterQuery]="executionDetailfilterParametersState$ | async"
        (onFilter)="filterParameters($event)">
      </sparta-execution-detail-table>
    </sparta-execution-detail-drop-down-title>
    <sparta-execution-detail-drop-down-title [title]="qualityRulesTitle" [isVisibleContent]="true">
      <sparta-execution-detail-table
        [isSearchable]="false"
        [defaultFilterLabel]="qualityRulesFilterLabel"
        [valueToFilter]="qualityRulesValueToFilter"
        [fields]="qualityRulesFields"
        [values]="qualityRulesState$ | async"
        (onSelectItem)="openQualityRulesDetail($event)">
      </sparta-execution-detail-table>
    </sparta-execution-detail-drop-down-title>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExecutionDetailTableContainer implements OnInit {

  @Output() onSelectQualityRule = new EventEmitter<any>();

  public executionDetailParametersState$: Observable<Array<Parameter>>;
  public executionDetailStatusesState$: Observable<Array<Status>>;
  public qualityRulesState$: Observable<Array<QualityRule>>;
  public executionDetailfilterParametersState$: Observable<string>;
  public parametersFields = [
    { id: 'name', label: 'Name', sortable: true },
    { id: 'type', label: 'Type', sortable: false },
    { id: 'lastModified', label: 'Value', sortable: false }
  ];
  public runtimeFields = [
    { id: 'name', label: 'Status', sortable: false },
    { id: 'statusInfo', label: 'Info', sortable: false },
    { id: 'startTime', label: 'Start time', sortable: true }
  ];

  public qualityRulesFields = [
    { id: 'name', label: 'Name', sortable: true },
    { id: 'threshold', label: 'Threshold', sortable: false },
    { id: 'qualityScore', label: 'Quality (%)', sortable: false },
    { id: 'satisfiedMessage', label: 'Quality Status', sortable: false, icon: 'satisfiedIcon'},
    { id: 'globalActionResume', label: 'Global action', sortable: false },
    { id: '', label: '', sortable: false, icon: 'warningIcon' }
  ];
  public parametersTitle = 'Parameters';
  public statusesTitle = 'States';
  public qualityRulesTitle = 'Quality rules';
  public qualityRulesFilterLabel = 'All rules';
  public qualityRulesValueToFilter = 'satisfiedMessage';

  constructor(private _store: Store<State>) {}

  ngOnInit(): void {
    this.executionDetailParametersState$ = this._store.pipe(select(executionDetailParametersState));
    this.executionDetailStatusesState$ = this._store.pipe(select(executionDetailStatusesState));
    this.executionDetailfilterParametersState$ = this._store.pipe(select(executionDetailfilterParametersState));
    this.qualityRulesState$ = this._store.pipe(select(qualityRulesState))
      .pipe(map(qualityRules => qualityRules.map(qualityRule => {
        qualityRule.globalActionResume = globalActionMap.get(qualityRule.globalAction);
        return qualityRule;
      })));
  }

  openQualityRulesDetail(qualityRuleId) {
    this.onSelectQualityRule.emit(qualityRuleId);
  }

  filterParameters(filter) {
    this._store.dispatch(new FilterParametersAction(filter));
  }

}
