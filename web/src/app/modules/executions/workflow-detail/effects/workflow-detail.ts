/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import {Action} from '@ngrx/store';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';
import * as workflowDetailActions from '../actions/workflow-detail';
import { switchMap, catchError, map } from 'rxjs/operators';
import { ExecutionService, InitializeWorkflowService } from 'services/execution.service';
import { ExecutionHelperService } from 'app/services/helpers/execution.service';

@Injectable()
export class WorkflowDetailEffect {

  @Effect()
  getWorkflowDetail: Observable<any> = this.actions$
    .pipe(ofType(workflowDetailActions.GET_WORKFLOW_DETAIL))
    .pipe(map((action: any) => action.executionId))
    .pipe(switchMap((executionId: string) => this._executionService.getExecutionById(executionId)
    .pipe(map((execution: any) => {
      execution.genericDataExecution.workflow = this._initializeWorkflowService.getInitializedWorkflow(execution.genericDataExecution.workflow).workflow;
      return new workflowDetailActions.GetWorkflowDetailCompleteAction(execution);
    }))
    /*.pipe(catchError(error => {
      return of(new workflowDetailActions.GetWorkflowDetailCompleteAction(execution));
    }))*/
    )
  );

  @Effect()
  getQualityRules$: Observable<Action> = this.actions$
    .pipe(
      ofType(workflowDetailActions.GET_QUALITY_RULES),
      map((action: any) => action.executionId),
      switchMap(executionId => this._executionService.getQualityRules(executionId)),
      map(response => {
        return this._executionHelperService.normalizeQualityRules(response);
      }),
      map(executionDetail => new workflowDetailActions.GetQualityRulesActionComplete(executionDetail))
    );


  constructor(
    private actions$: Actions,
    private _executionService: ExecutionService,
    private _initializeWorkflowService: InitializeWorkflowService,
    private _executionHelperService: ExecutionHelperService
  ) { }

}
