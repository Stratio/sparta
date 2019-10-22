/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import * as workflowDetailActions from './actions/workflow-detail-repo';
import {select, Store} from '@ngrx/store';
import * as workflowDetailReducer from './reducers/index';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';
import {WizardNode} from '@app/wizard/models/node';
import {SelectedNodeOutputNames} from '@app/shared/components/sp-workflow-detail/models';

@Component({
  selector: 'workflow-detail-repo-container',
  template: `<workflow-detail
                [id]="id"
                [nodes]="nodes$ | async"
                [edges]="edges$ | async"
                [workflowName]="workflowName$ | async"
                [ciCdLabel]="ciCdLabel$ | async"
                [executionEngine]="executionEngine$ | async"
                [uiSettings]="uiSettings$ | async"
                [showConfigModal]="showConfigModal$ | async"
                [selectedNodeOutputNames]="selectedNodeOutputNames$ | async"
                (onSelectEdge)="selectEdge($event)"
                (onOpenNode)="openNode($event)"
                (closeModal)="closeModal()"
             ></workflow-detail>`,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowDetailRepoContainer implements OnInit {

  public id: string;
  // public isLoading$: Observable<WorkflowDetail>;
  public workflowName$: Observable<string>;
  public executionEngine$: Observable<string>;
  public ciCdLabel$: Observable<string>;
  public edges$: Observable<Array<any>>;
  public nodes$: Observable<Array<any>>;
  public uiSettings$: Observable<Array<any>>;
  public selectedNode$: Observable<WizardNode>;
  public showConfigModal$: Observable<boolean>;
  public selectedNodeOutputNames$: Observable<SelectedNodeOutputNames>;

  constructor(
    private _route: ActivatedRoute,
    private _store: Store<workflowDetailReducer.State>,
    private _cd: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.id = this._route.snapshot.params.id;
    this._store.dispatch(new workflowDetailActions.GetWorkflowDetailAction(this.id));
    this.nodes$ = this._store.pipe(select(workflowDetailReducer.getWorkflowNodes));
    this.edges$ = this._store.pipe(select(workflowDetailReducer.getEdgesMap));
    this.workflowName$ = this._store.pipe(select(workflowDetailReducer.getWorkflowName));
    this.executionEngine$ = this._store.pipe(select(workflowDetailReducer.getWorkflowExecutionEngine));
    this.uiSettings$ = this._store.pipe(select(workflowDetailReducer.getWorkflowUISettings));
    this.ciCdLabel$ = this._store.pipe(select(workflowDetailReducer.getWorkflowCICDLabel));

    this.selectedNode$ = this._store.pipe(select(workflowDetailReducer.getSelectedNode));
    this.selectedNodeOutputNames$ = this._store.pipe(select(workflowDetailReducer.getSelectedNodeOutputNames));
    this.showConfigModal$ = this._store.pipe(select(workflowDetailReducer.getShowModal));
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

}
