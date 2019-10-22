/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import * as workflowDetailActions from './actions/workflow-detail-execution';
import {select, Store} from '@ngrx/store';
import * as workflowDetailReducer from './reducers/index';
import {QualityRule} from '@app/executions/models';
import {ActivatedRoute} from '@angular/router';
import {Observable, Subject} from 'rxjs';
import {WorkflowDetail} from './reducers/index';
import {WizardNode} from '@app/wizard/models/node';
import {SelectedNodeOutputNames} from '@app/shared/components/sp-workflow-detail/models';

@Component({
  selector: 'workflow-detail-execution-container',
  template: `
      <workflow-detail
        [id]="id"
        [nodes]="nodes$ | async"
        [edges]="edges$ | async"
        [workflowName]="workflowName"
        [executionEngine]="executionEngine"
        [uiSettings]="uiSettings"
        [filteredQualityRules]="filteredQualityRules$ | async"
        [qualityRulesCount]="qualityRulesCount$ | async"
        [qualityRulesStatus]="qualityRulesStatus$ | async"
        [showConfigModal]="showConfigModal$ | async"
        [selectedNodeOutputNames]="selectedNodeOutputNames$ | async"
        (onSelectEdge)="selectEdge($event)"
        (onOpenNode)="openNode($event)"
        (closeModal)="closeModal()"
      ></workflow-detail>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowDetailExecutionContainer implements OnInit, OnDestroy {

  public id: string;

  public isLoading$: Observable<WorkflowDetail>;
  public workflowName: string;
  public executionEngine: string;
  public uiSettings: any;
  public edges$: Observable<Array<any>>;
  public nodes$: Observable<Array<any>>;
  public filteredQualityRules$: Observable<Array<QualityRule>>;
  public qualityRulesCount$: Observable<Array<number>>;
  public qualityRulesStatus$: Observable<Array<boolean>>;

  public selectedNode$: Observable<WizardNode>;
  public showConfigModal$: Observable<boolean>;
  public selectedNodeOutputNames$: Observable<SelectedNodeOutputNames>;

  private _componentDestroyed = new Subject();

  constructor(
    private _route: ActivatedRoute,
    private _store: Store<workflowDetailReducer.State>,
    private _cd: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.id = this._route.snapshot.params.id;
    this._store.dispatch(new workflowDetailActions.GetWorkflowDetailAction(this.id));
    this._store.dispatch(new workflowDetailActions.GetQualityRulesAction(this.id));

    this.edges$ = this._store.pipe(select(workflowDetailReducer.getEdgesMap));
    this.nodes$ = this._store.pipe(select(workflowDetailReducer.getWorkflowNodes));
    this.isLoading$ = this._store.pipe(select(workflowDetailReducer.getWorkflowDetailIsLoading));
    this.filteredQualityRules$ = this._store.pipe(select(workflowDetailReducer.filteredQualityRulesState));
    this.qualityRulesCount$ = this._store.pipe(select(workflowDetailReducer.getQualityRulesCount));
    this.qualityRulesStatus$ = this._store.pipe(select(workflowDetailReducer.getQualityRulesStatus));

    this.selectedNode$ = this._store.pipe(select(workflowDetailReducer.getSelectedNode));
    this.selectedNodeOutputNames$ = this._store.pipe(select(workflowDetailReducer.getSelectedNodeOutputNames));
    this.showConfigModal$ = this._store.pipe(select(workflowDetailReducer.getShowModal));

    this._store.pipe(select(workflowDetailReducer.getExecution))
      .subscribe(execution => {
        if (execution) {
          this.workflowName = execution.genericDataExecution.workflow.name;
          this.executionEngine = execution.executionEngine;
          this.uiSettings = execution.genericDataExecution.workflow.uiSettings;
          this._cd.markForCheck();
        }
      });
  }

  selectEdge(edge) {
    this._store.dispatch(new workflowDetailActions.SelectEdgeAction(edge));
  }

  openNode(node: WizardNode) {
    this._store.dispatch(new workflowDetailActions.ShowConfigModal(node));
  }

  public closeModal() {
    this._store.dispatch(new workflowDetailActions.HideConfigModal());
  }

  ngOnDestroy(): void {
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
  }

}
