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
import { Subscription } from 'rxjs/Rx';
import { ActivatedRoute, Router } from '@angular/router';

import { WorkflowGroupModal } from './components/workflow-group-modal/workflow-group-modal.component';
import { WorkflowJsonModal } from './components/workflow-json-modal/workflow-json-modal.component';
import * as fromRoot from 'reducers';
import * as workflowActions from './actions/workflow-list';

@Injectable()
export class WorkflowsManagingService {

    public workflowModalTitle: string;
    public workflowModalCt: string;
    public workflowJsonModalTitle: string;

    public modalSubscription: Subscription;
    public createGroupModalTitle: string;

    public showCreateJsonModal(): void {
        this._modalService.show({
            modalTitle: this.workflowJsonModalTitle,
            maxWidth: 980,
            outputs: {
                onCloseJsonModal: this.onCloseJsonModal.bind(this)
            },
        }, WorkflowJsonModal);
    }

    public runWorkflow(versionId: string, workflowName: string): void {
        this.store.dispatch(new workflowActions.RunWorkflowAction({
            id: versionId,
            name: workflowName
        }));
    }

    public stopWorkflow(workflowStatus: any): void {
        this.store.dispatch(new workflowActions.StopWorkflowAction(workflowStatus));
    }

    public onCloseJsonModal(action: any) {
        this._modalService.close();
    }

    public createWorkflowGroup(): void {
        this._modalService.show({
            modalTitle: this.createGroupModalTitle,
            maxWidth: 500,
            outputs: {
                onCloseGroupModal: () => this._modalService.close()
            },
        }, WorkflowGroupModal);
    }

    constructor(private store: Store<fromRoot.State>, private _modalService: StModalService, private translate: TranslateService,
        private route: Router, private currentActivatedRoute: ActivatedRoute) {
        const workflowModalCt = 'DASHBOARD.NEW_WORKFLOW';
        const createGroupModalTitle = 'DASHBOARD.CREATE_GROUP_TITLE';
        const workflowModalTitle = 'DASHBOARD.CHOOSE_METHOD';
        const workflowJsonModalTitle = 'DASHBOARD.JSON_TITLE';

        this.translate.get([workflowModalCt, workflowModalTitle, workflowJsonModalTitle, createGroupModalTitle]).subscribe(
            (value: { [key: string]: string }) => {
                this.workflowModalCt = value[workflowModalCt].toUpperCase();
                this.workflowModalTitle = value[workflowModalTitle];
                this.workflowJsonModalTitle = value[workflowJsonModalTitle];
                this.createGroupModalTitle = value[createGroupModalTitle];
            });
    }
}
