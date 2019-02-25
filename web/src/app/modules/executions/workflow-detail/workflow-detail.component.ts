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
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';


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

   private _componentDestroyed = new Subject();

   constructor(private _route: ActivatedRoute, private _store: Store<workflowDetailReducer.State>, private _cd: ChangeDetectorRef) { }

   ngOnInit() {
      const id = this._route.snapshot.params.id;
      this._store.dispatch(new workflowDetailActions.GetWorkflowDetailAction(id));
      this._store.pipe(select(workflowDetailReducer.getWorkflowDetail))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((workflow: any) => {
         const execution = workflow.execution;

         if (execution) {
            this.execution = execution.execution;
            const { pipelineGraph } =  execution.execution.genericDataExecution.workflow;
            this.nodes = pipelineGraph.nodes;
            this.edges = this.getEdgesMap(pipelineGraph.nodes, pipelineGraph.edges);
         }
         this._cd.markForCheck();
      });

      this._store.pipe(select(workflowDetailReducer.getWorkflowDetailIsLoading))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((isLoading: any) => {
         this.isLoading = isLoading.loading;
         this._cd.markForCheck();
      });
   }

   getEdgesMap(nodes: Array<any>, edges: Array<any>) {
    const nodesMap = nodes.reduce(function (map, obj) {
      map[obj.name] = obj;
      return map;
    }, {});
    return edges.map((edge: any) => ({
      origin: nodesMap[edge.origin],
      destination: nodesMap[edge.destination],
      dataType: edge.dataType
    }));
  }

  selectStep(stepName: string) {
    this.selectedStep = stepName;
  }

  getTypeof(property: any) {
    return typeof property;
  }

  ngOnDestroy(): void {
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
 }

}
