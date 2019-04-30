/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild, ViewContainerRef, EventEmitter, Output } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import {
   State,
   qualityRulesState
} from './reducers';
import { QualityRule, globalActionMap } from '@app/executions/models';
import * as executionDetailActions from './actions/execution-detail';
import {BreadcrumbMenuService} from 'services';
import { StModalService } from '@stratio/egeo';


@Component({
   selector: 'sparta-execution-detail',
   styleUrls: ['execution-detail.styles.scss'],
   templateUrl: 'execution-detail.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionDetailComponent implements OnInit, OnDestroy {

  @ViewChild('executionDetailModal', { read: ViewContainerRef }) target: any;

  public breadcrumbOptions: string[] = [];
  public showQualityRuleDetail = false;
  public qualityRule = {};
  public returnTo = 'Execution detail';
  public qualityRules$: Observable<QualityRule>;
  public modalTitle = 'Quality Rules';

  constructor(private _route: ActivatedRoute,
    private _store: Store<State>,
    public breadcrumbMenuService: BreadcrumbMenuService,
    private _stModalService: StModalService) {
    this.breadcrumbOptions = breadcrumbMenuService.getOptions();
  }

  ngOnInit() {
    this._stModalService.container = this.target;
    const executionId = this._route.snapshot.params.id;
    this._store.dispatch(new executionDetailActions.ResetExecutionDetail());
    this._store.dispatch(new executionDetailActions.GetExecutionDetailAction(executionId));
    this._store.dispatch(new executionDetailActions.GetQualityRulesAction(executionId));
  }

  toggleQualityRuleData() {
    this.showQualityRuleDetail = !this.showQualityRuleDetail;
  }

  loadQualityRules(qualityRuleId) {
    this.qualityRules$ = this._store.pipe(
      select(qualityRulesState),
      map(qualityRules => qualityRules.map(qualityRule => {
        qualityRule.globalActionResume = globalActionMap.get(qualityRule.globalAction);
        return qualityRule;
      })),
      map(qualityRules => qualityRules.find(item => item.id === qualityRuleId))
    );
    this.toggleQualityRuleData();
  }

  ngOnDestroy(): void {
    this._store.dispatch(new executionDetailActions.CancelPollingAction());
    this._store.dispatch(new executionDetailActions.CancelPollingQualityRulesAction());
  }

}
