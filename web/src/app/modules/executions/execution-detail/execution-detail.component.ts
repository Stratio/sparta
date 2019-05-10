/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild, ViewContainerRef, EventEmitter, Output, ChangeDetectorRef } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import * as executionDetailsAction from './reducers';

import {
  State,
  qualityRulesState
} from './reducers';
import { QualityRule, globalActionMap } from '@app/executions/models';
import * as executionDetailActions from './actions/execution-detail';
import { BreadcrumbMenuService } from 'services';
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
  public showDebugConsole: boolean;
  public lastError: any;


  private _componentDestroyed = new Subject();

  constructor(private _route: ActivatedRoute,
    private _store: Store<State>,
    private _cd: ChangeDetectorRef,
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
    this._store.pipe(select(executionDetailsAction.showConsole))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(showConsole => {
        this.showDebugConsole = showConsole;
        this._cd.markForCheck();
      });

    this._store.pipe(select(executionDetailsAction.getLastExecutionError))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(lastError => {
        this.lastError = lastError;
        this._cd.markForCheck();
      });
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

  onCloseConsole() {
    this._store.dispatch(new executionDetailActions.HideConsoleAction());
  }

  ngOnDestroy(): void {
    this._store.dispatch(new executionDetailActions.CancelPollingAction());
    this._store.dispatch(new executionDetailActions.CancelPollingQualityRulesAction());
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
  }

}
