/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';


import * as fromExecution from '../../reducers';
import * as executionActions from '../../actions/executions';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
   selector: 'executions-detail',
   templateUrl: './executions-detail.template.html',
   styleUrls: ['./executions-detail.styles.scss']
})
export class ExecutionsDetailComponent implements OnInit, OnDestroy {

   public isLoading: boolean;
   public execution: any;
   public edges: any = [];
   public nodes: any = [];

   private _componentDestroyed = new Subject();

   constructor(private _route: ActivatedRoute, private _store: Store<fromExecution.State>, private _cd: ChangeDetectorRef) { }

   ngOnInit() {
      const id = this._route.snapshot.params.id;
      this._store.dispatch(new executionActions.GetExecutionAction(id));
      this._store.select(fromExecution.getExecutionDetailInfo)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(execution => {
         this.execution = execution;
         if (execution) {
            const { pipelineGraph } =  execution.genericDataExecution.workflow;
            this.nodes = pipelineGraph.nodes;
            this.edges = this.getEdgesMap(pipelineGraph.nodes, pipelineGraph.edges);
         }
         this._cd.markForCheck();
      });

      this._store.select(fromExecution.getExecutionDetailIsLoading)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(isLoading => {
         this.isLoading = isLoading;
         this._cd.markForCheck();
      });
   }

   getEdgesMap(nodes, edges) {
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

  ngOnDestroy(): void {
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
 }
}
