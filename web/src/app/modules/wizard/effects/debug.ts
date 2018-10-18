/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Observable, of, from, timer } from 'rxjs';

import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';
import { Effect, Actions, ofType } from '@ngrx/effects';
import { withLatestFrom, switchMap, map, mergeMap, flatMap, concatMap } from 'rxjs/operators';

import * as fromWizard from './../reducers';
import * as wizardActions from './../actions/wizard';
import * as debugActions from './../actions/debug';
import * as errorActions from 'actions/errors';

import { WizardService } from '@app/wizard/services/wizard.service';
import { WizardApiService, WorkflowService } from 'app/services';
import { getWorkflowId } from './../reducers';

@Injectable()
export class DebugEffect {

   @Effect()
   getExecutionContexts$: Observable<Action> = this._actions$
      .pipe(ofType<debugActions.ConfigAdvancedExecutionAction>(debugActions.CONFIG_ADVANCED_EXECUTION))
      .pipe(withLatestFrom(this._store.select(state => state)))
      .pipe(switchMap(([action, state]) =>
         this._workflowService.getRunParametersFromWorkflow(this._wizardService.getWorkflowModel(state))
            .pipe(map(response => new debugActions.ConfigAdvancedExecutionCompleteAction(response)))
            .catch(error => of(new debugActions.ConfigAdvancedExecutionErrorAction()))));

   @Effect()
   debugWorkflow$: Observable<any> = this._actions$
      .pipe(ofType(debugActions.INIT_DEBUG_WORKFLOW))
      .pipe(map((action: any) => action.config))
      // Retrieve part of the current state
      .pipe(withLatestFrom(this._store.select(state => state)))
      .pipe(switchMap(([config, state]: [any, any]) => {
         let workflow = this._wizardService.getWorkflowModel(state);
         const nodes = workflow.pipelineGraph.nodes.map(node => {
            const actualNode = workflow.pipelineGraph.nodes.filter(n => n.name === node.name)[0];
            if (node.classPrettyName === 'QueryBuilder' && actualNode && actualNode.configuration.visualQuery && actualNode.configuration.visualQuery.joinClause && actualNode.configuration.visualQuery.joinClause.joinConditions && actualNode.configuration.visualQuery.joinClause.joinConditions.length && workflow.pipelineGraph.edges.filter(edge => edge.destination === node.name).length === 1) {
               const fromClause = {
                  tableName: workflow.pipelineGraph.edges.filter(edge => edge.destination === node.name)[0].origin,
                  alias: 't1'
               };
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
            .pipe(flatMap((response) => (config ?
               this._wizardApiService.debugWithExecutionContext(response.workflowDebug.id || response.workflowOriginal.id, config) :
               this._wizardApiService.runDebug(response.workflowDebug.id || response.workflowOriginal.id))
               .pipe(mergeMap(res => [
                  new debugActions.InitDebugWorkflowCompleteAction(response.workflowDebug.id || response.workflowOriginal.id),
                  new wizardActions.ShowNotificationAction({
                     type: 'default',
                     templateType: 'runDebug',
                     time: 0
                  }),
                  ...workflow.id && workflow.id.length ? [] : [
                     new wizardActions.SetWorkflowIdAction(response.workflowDebug.id || response.workflowOriginal.id)
                  ]]))))
            .catch(error => from([new debugActions.InitDebugWorkflowErrorAction(), new errorActions.ServerErrorAction(error)]));

      }));

   @Effect()
   pollingDebugContext$: Observable<any> = this._actions$
      .pipe(ofType(debugActions.INIT_DEBUG_WORKFLOW_COMPLETE))
      .pipe(map((action: any) => action.payload))
      .pipe(switchMap((workflowId: string) => timer(0, 2000)
         .takeUntil(this._actions$.ofType(debugActions.CANCEL_DEBUG_POLLING))
         .pipe(concatMap(() => this._wizardApiService.getDebugResult(workflowId)
            .pipe(mergeMap((result: any) => [
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
            ])).catch(error => of(new debugActions.GetDebugResultErrorAction()))))
      ));


   @Effect()
   getDebugResult$: Observable<any> = this._actions$
      .pipe(ofType(debugActions.GET_DEBUG_RESULT))
      .pipe(map((action: any) => action.payload))
      .pipe(switchMap((workflowId: string) => this._wizardApiService.getDebugResult(workflowId)
         .pipe(map(result => new debugActions.GetDebugResultCompleteAction(result)))
         .catch(error => of(new debugActions.GetDebugResultErrorAction())))
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
      .pipe(ofType(debugActions.DELETE_DEBUG_FILE))
      .pipe(map((action: any) => action.fileName))
      .pipe(switchMap((path: string) => this._wizardApiService.deleteDebugFile(path)
         .pipe(map(response => new debugActions.DeleteDebugFileCompleteAction()))
         .catch(error => of(new debugActions.DeleteDebugFileErrorAction()))));


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
      private _workflowService: WorkflowService,
      private _wizardApiService: WizardApiService
   ) { }


}

