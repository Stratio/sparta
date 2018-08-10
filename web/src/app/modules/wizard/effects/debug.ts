/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/concatMap';
import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/from';
import 'rxjs/add/operator/delay';
import 'rxjs/add/observable/if';
import 'rxjs/add/observable/empty';
import 'rxjs/add/observable/timer';
import 'rxjs/add/observable/throw';
import { of } from 'rxjs/observable/of';
import { Observable } from 'rxjs/Observable';

import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Effect, Actions, toPayload } from '@ngrx/effects';

import * as fromWizard from './../reducers';
import * as wizardActions from './../actions/wizard';
import * as debugActions from './../actions/debug';
import { WizardService } from '@app/wizard/services/wizard.service';
import { WizardApiService } from 'app/services';
import { getWorkflowId } from './../reducers';

@Injectable()
export class DebugEffect {

   @Effect()
   debugWorkflow$: Observable<any> = this._actions$
      .ofType(debugActions.INIT_DEBUG_WORKFLOW)
      .map(toPayload)
      // Retrieve part of the current state
      .withLatestFrom(this._store.select(state => state))
      .switchMap(([redirectOnSave, state]: [any, any]) => {
         let workflow = this._wizardService.getWorkflowModel(state);
         const nodes = workflow.pipelineGraph.nodes.map(node => {
            const actualNode = workflow.pipelineGraph.nodes.filter(n => n.name === node.name)[0];
            if (node.classPrettyName === 'QueryBuilder' && actualNode && actualNode.configuration.visualQuery && actualNode.configuration.visualQuery.joinClause  && actualNode.configuration.visualQuery.joinClause.joinConditions.length  && workflow.pipelineGraph.edges.filter(edge => edge.destination === node.name).length === 1) {
               const fromClause = {
                  tableName: workflow.pipelineGraph.edges.filter(edge => edge.destination === node.name)[0].origin,
                  alias: 't1'
               }
               return {
                  ...node,
                  configuration: {
                     visualQuery: {
                        ...node.configuration.visualQuery,
                        joinClause: {},
                        fromClause,
                        selectClauses: []
                     }
                  }
               };
            } else {
               return node;
            }
         });
         workflow = {
            ...workflow,
            pipelineGraph: {
               ...workflow.pipelineGraph,
               nodes
            }
         };
         return this._wizardApiService.debug(workflow)
            .flatMap((response) => this._wizardApiService.runDebug(response.workflowDebug.id || response.workflowOriginal.id)
               .mergeMap(res => [
                  new debugActions.InitDebugWorkflowCompleteAction(response.workflowDebug.id || response.workflowOriginal.id),
                  new wizardActions.ShowNotificationAction({
                     type: 'default',
                     templateType: 'runDebug',
                     time: 0
                  }),
                  ... workflow.id && workflow.id.length ? [] : [
                        new wizardActions.SetWorkflowIdAction(response.workflowDebug.id || response.workflowOriginal.id)
                  ]]))
            .catch(error => of(new debugActions.InitDebugWorkflowErrorAction()));

      });

   @Effect()
   pollingDebugContext$: Observable<any> = this._actions$
      .ofType(debugActions.INIT_DEBUG_WORKFLOW_COMPLETE)
      .map((action: any) => action.payload)
      .switchMap((workflowId: string) => Observable.timer(0, 2000)
         .takeUntil(this._actions$.ofType(debugActions.CANCEL_DEBUG_POLLING))
         .concatMap(() => this._wizardApiService.getDebugResult(workflowId)
            .mergeMap(result => [
               new debugActions.CancelDebugPollingAction(),
               new debugActions.GetDebugResultCompleteAction(result),
               new wizardActions.ShowNotificationAction(result.debugSuccessful ? {
                  type: 'default',
                  templateType: 'debugSuccess'
               } : result.genericError && result.genericError.message ? {
                  type: 'default',
                  templateType: 'generic'
               } : {
                        type: 'default',
                        templateType: 'debugFail'
                     })
            ])
            .catch(error => of(new debugActions.GetDebugResultErrorAction()))
         ));


   @Effect()
   getDebugResult$: Observable<any> = this._actions$
      .ofType(debugActions.GET_DEBUG_RESULT)
      .map((action: any) => action.payload)
      .switchMap((workflowId: string) => this._wizardApiService.getDebugResult(workflowId)
         .map(result => new debugActions.GetDebugResultCompleteAction(result))
         .catch(error => of(new debugActions.GetDebugResultErrorAction()))
      );

   @Effect()
   uploadDebugFile$: Observable<any> = this._actions$
      .ofType(debugActions.UPLOAD_DEBUG_FILE)
      .map((action: any) => action.payload)
      .withLatestFrom(this._store.select(getWorkflowId))
      .switchMap(([file, workflowId]: [any, string]) => this._wizardApiService.uploadDebugFile(workflowId, file)
         .map(response => new debugActions.UploadDebugFileCompleteAction(response[0].path))
         .catch(error => of(new debugActions.UploadDebugFileErrorAction())));


   @Effect()
   deleteDebugFile$: Observable<any> = this._actions$
      .ofType(debugActions.DELETE_DEBUG_FILE)
      .map((action: any) => action.fileName)
      .switchMap((path: string) => this._wizardApiService.deleteDebugFile(path)
         .map(response => new debugActions.DeleteDebugFileCompleteAction())
         .catch(error => of(new debugActions.DeleteDebugFileErrorAction())));


   @Effect()
   downloadDebugFile$: Observable<any> = this._actions$
      .ofType(debugActions.DOWNLOAD_DEBUG_FILE)
      .map((action: any) => action.fileName)
      .switchMap((path: string) => this._wizardApiService.downloadDebugFile(path)
         .map(response => new debugActions.DownloadDebugFileCompleteAction())
         .catch(error => of(new debugActions.DownloadDebugFileErrorAction())));

   constructor(
      private _actions$: Actions,
      private _store: Store<fromWizard.State>,
      private _wizardService: WizardService,
      private _wizardApiService: WizardApiService
   ) { }


}

