/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, ChangeDetectorRef, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Store, select } from '@ngrx/store';

import * as workflowDetailReducer from './reducers';

import * as workflowDetailActions from './actions/workflow-detail';
import { Subject, Observable } from 'rxjs';
import { takeUntil, map } from 'rxjs/operators';
import { StHorizontalTab } from '@stratio/egeo';
import { QualityRule, globalActionMap, Edge } from '@app/executions/models';

import { batchInputsObject, streamingInputsObject } from 'data-templates/inputs';
import { batchOutputsObject, streamingOutputsObject } from 'data-templates/outputs';
import { batchTransformationsObject, streamingTransformationsObject } from 'data-templates/transformations';
import { Engine } from '@models/enums';
import { WizardNode } from '@app/wizard/models/node';

@Component({
  selector: 'workflow-detail',
  templateUrl: './workflow-detail.template.html',
  styleUrls: ['./workflow-detail.styles.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowDetailComponent implements OnInit, OnDestroy {

  public isLoading: boolean;
  public execution: any;
  public edges: any = [];
  public nodes: any = [];
  public selectedStep: any;
  public keys = Object.keys;
  public id: string;
  public selectedQualityRules = false;
  public qualityRules$: Observable<Array<QualityRule>>;
  public selectedEdge$: Observable<Edge>;
  public selectedNode$: Observable<WizardNode>;
  public showConfigModal$: Observable<boolean>;
  public selectedNodeOutputNames$: Observable<any>;
  public filteredQualityRules: Array<QualityRule> = [];
  public filteredQualityRules$: Observable<any>;
  public currentStepTemplate: any;
  public tabOptions: StHorizontalTab[] = [
    { id: 'global', text: 'Global' },
    { id: 'writer', text: 'Writer' }
  ];
  public tabSelectedOption: StHorizontalTab = this.tabOptions[0];
  public qualityRulesCount: Array<number>;
  public qualityRulesStatus: Array<boolean>;

  private _componentDestroyed = new Subject();

  constructor(private _route: ActivatedRoute, private _store: Store<workflowDetailReducer.State>, private _cd: ChangeDetectorRef) { }

  ngOnInit() {
    this.id = this._route.snapshot.params.id;
    this._store.dispatch(new workflowDetailActions.GetWorkflowDetailAction(this.id));
    this._store.dispatch(new workflowDetailActions.GetQualityRulesAction(this.id));

    this._store.pipe(select(workflowDetailReducer.getWorkflowDetail))
    .pipe(takeUntil(this._componentDestroyed))
    .subscribe((workflow: any) => {
      const execution = workflow.execution;

      if (execution) {
        this.execution = execution.execution;
        const { pipelineGraph } = execution.execution.genericDataExecution.workflow;
      }
      this._cd.markForCheck();
    });

    this._store.pipe(select(workflowDetailReducer.getEdgesMap))
    .pipe(takeUntil(this._componentDestroyed))
    .subscribe((edgesMap: any) => {
      this.edges = edgesMap;
      this._cd.markForCheck();
    });


    this._store.pipe(select(workflowDetailReducer.getWorkflowNodes))
    .pipe(takeUntil(this._componentDestroyed))
    .subscribe((nodes: any) => {
      this.nodes = nodes;
      this._cd.markForCheck();
    });

    this._store.pipe(select(workflowDetailReducer.getWorkflowDetailIsLoading))
    .pipe(takeUntil(this._componentDestroyed))
    .subscribe((isLoading: any) => {
      this.isLoading = isLoading.loading;
      this._cd.markForCheck();
    });


    this.selectedEdge$ = this._store.pipe(select(workflowDetailReducer.selectedEdgeState));
    this.selectedNode$ = this._store.pipe(select(workflowDetailReducer.getSelectedNode));
    this.selectedNodeOutputNames$ = this._store.pipe(select(workflowDetailReducer.getSelectedNodeOutputNames));
    this.showConfigModal$ = this._store.pipe(select(workflowDetailReducer.getShowModal));

    this.qualityRules$ = this._store.pipe(
      select(workflowDetailReducer.qualityRulesState),
      map(qualityRules => qualityRules.map(qualityRule => {
        qualityRule.globalActionResume = globalActionMap.get(qualityRule.globalAction);
        return qualityRule;
      }))
    );

    this._store.pipe(select(workflowDetailReducer.filteredQualityRulesState))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(qualityRules => {
        this.filteredQualityRules = qualityRules;
        this._cd.markForCheck();
      });

    this._store.select(workflowDetailReducer.getQualityRulesCount)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(qualityRulesCount => {
        this.qualityRulesCount = qualityRulesCount;
        this._cd.markForCheck();
      });

    this._store.select(workflowDetailReducer.getQualityRulesStatus)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(qualityRulesStatus => {
        this.qualityRulesStatus = qualityRulesStatus;
        this._cd.markForCheck();
      });
  }

  selectStep(step: any) {
    this.selectedStep = step;
    this.tabSelectedOption = this.tabOptions[0];
    this.selectedQualityRules = false;

    if (step.executionEngine === Engine.Batch) {
      switch (step.stepType) {
        case 'Input':
          this.currentStepTemplate = batchInputsObject[step.classPrettyName];
          break;
        case 'Output':
          this.currentStepTemplate = batchOutputsObject[step.classPrettyName];
          break;
        case 'Transformation':
          this.currentStepTemplate = batchTransformationsObject[step.classPrettyName];
          break;
      }
    } else {
      switch (step.stepType) {
        case 'Input':
          this.currentStepTemplate = streamingInputsObject[step.classPrettyName];
          break;
        case 'Output':
          this.currentStepTemplate = streamingOutputsObject[step.classPrettyName];
          break;
        case 'Transformation':
          this.currentStepTemplate = streamingTransformationsObject[step.classPrettyName];
          break;
      }
    }
  }

  getTypeof(property: any) {
    return typeof property;
  }

  changedOption(option: StHorizontalTab) {
    this.tabSelectedOption = option;
  }

  ngOnDestroy(): void {
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
  }

  selectEdge(edge) {
    this._store.dispatch(new workflowDetailActions.SelectEdgeAction(edge));
  }

  openNode(node: WizardNode) {
    this._store.dispatch(new workflowDetailActions.ShowConfigModal(node));
  }

}
