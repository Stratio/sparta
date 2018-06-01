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

@Injectable()
export class DebugEffect {

   @Effect()
   debugWorkflow$: Observable<any> = this._actions$
      .ofType(debugActions.INIT_DEBUG_WORKFLOW)
      .map(toPayload)
      // Retrieve part of the current state
      .withLatestFrom(this._store.select(state => state))
      .switchMap(([redirectOnSave, state]: [any, any]) => {
         const workflow = this._wizardService.getWorkflowModel(state);
         return this._wizardApiService.debug(workflow)
            .flatMap((response) => this._wizardApiService.runDebug(response.workflowDebug.id)
               .mergeMap(res => [
                  new debugActions.InitDebugWorkflowCompleteAction(response.workflowDebug.id),
                  new wizardActions.ShowNotificationAction({
                     type: 'default',
                     message: 'DEBUG_RUN'
                  })]))
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
                  type: 'success',
                  message: 'DEBUG_SUCCESS'
               } : result.genericError && result.genericError.message ? {
                  type: 'critical',
                  message: 'DEBUG_GENERIC_FAIL'
               } : {
                     type: 'critical',
                     message: 'DEBUG_FAIL'
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

   constructor(
      private _actions$: Actions,
      private _store: Store<fromWizard.State>,
      private _wizardService: WizardService,
      private _wizardApiService: WizardApiService
   ) { }


}

