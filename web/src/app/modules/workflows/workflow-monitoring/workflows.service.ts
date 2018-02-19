///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { StModalService } from '@stratio/egeo';
import * as fromRoot from 'reducers';
import * as workflowActions from './actions/workflow-list';
import { Subscription } from 'rxjs/Rx';
import { ActivatedRoute, Router } from '@angular/router';

@Injectable()
export class WorkflowsService {


    public modalSubscription: Subscription;


    public runWorkflow(workflowId: string, workflowName: string): void {
        this.store.dispatch(new workflowActions.RunWorkflowAction({
            id: workflowId,
            name: workflowName
        }));
    }

    public stopWorkflow(workflowStatus: any): void {
        this.store.dispatch(new workflowActions.StopWorkflowAction(workflowStatus));
    }

    constructor(private store: Store<fromRoot.State>, private _modalService: StModalService, private translate: TranslateService,
        private route: Router, private currentActivatedRoute: ActivatedRoute) {

    }
}
