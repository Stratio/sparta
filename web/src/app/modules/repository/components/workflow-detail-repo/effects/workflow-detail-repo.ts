/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import {Observable, of} from 'rxjs';
import * as workflowDetailActions from '../actions/workflow-detail-repo';
import {switchMap, map, catchError} from 'rxjs/operators';
import {ExecutionService, InitializeWorkflowService, WorkflowService} from 'services/execution.service';
import { ExecutionHelperService } from 'app/services/helpers/execution.service';
import {Action} from '@ngrx/store';

@Injectable()
export class WorkflowDetailRepoEffect {

  @Effect()
  getWorkflowDetail: Observable<Action> = this.actions$
    .pipe(ofType(workflowDetailActions.GET_WORKFLOW_DETAIL))
    .pipe(map((action: any) => action.versionId))
    .pipe(switchMap((id: any) => this._workflowService.getWorkflowById(id)
      .pipe(switchMap((response: any) => {
        const { workflow, writers } = this._initializeWorkflowService.getInitializedWorkflow(response);
        return [
          new workflowDetailActions.GetWorkflowDetailCompleteAction(workflow),
        ];
      })).pipe(catchError(error => {
        return of(new workflowDetailActions.GetWorkflowDetailErrorAction());
      }))));

  constructor(
    private actions$: Actions,
    private _executionService: ExecutionService,
    private _workflowService: WorkflowService,
    private _initializeWorkflowService: InitializeWorkflowService,
    private _executionHelperService: ExecutionHelperService
  ) { }

}
