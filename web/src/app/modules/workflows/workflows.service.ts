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
import { StTableHeader, StModalService, StModalResponse, StModalButton } from '@stratio/egeo';
import * as fromRoot from 'reducers';
import * as workflowActions from 'actions/workflow';
import { WorkflowJsonModal } from './components/workflow-json-modal/workflow-json-modal.component';
import { Subscription } from 'rxjs/Rx';
import { ActivatedRoute, Router } from '@angular/router';

@Injectable()
export class WorkflowsService {

    public workflowModalTitle: string;
    public workflowModalCt: string;
    public workflowJsonModalTitle: string;
    public deleteWorkflowModalTitle: string;
    public deleteWorkflowModalMessage: string;
    public modalSubscription: Subscription;
    public messageDeleteTitle: string;

    public setModalContainer(target: any): void {
        this._modalService.container = target;
    }

    public showCreateJsonModal(): void {
        this._modalService.show({
            modalTitle: this.workflowJsonModalTitle,
            maxWidth: 980,
            outputs: {
                onCloseJsonModal: this.onCloseJsonModal.bind(this)
            },
        }, WorkflowJsonModal);
    }

    public deleteWorkflowConfirmModal(workflows: Array<any>): void {
        const buttons: StModalButton[] = [
            { label: 'Cancel', classes: 'button-secondary-gray', responseValue: StModalResponse.NO },
            { label: 'Delete', classes: 'button-critical', responseValue: StModalResponse.YES, closeOnClick: true }
        ];
        this.modalSubscription = this._modalService.show({
            messageTitle: this.deleteWorkflowModalMessage,
            modalTitle: this.deleteWorkflowModalTitle,
            buttons: buttons,
            maxWidth: 500,
            message: this.messageDeleteTitle,
        }).subscribe((response: any) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                this.store.dispatch(new workflowActions.DeleteWorkflowAction(workflows));
            }
        });
    }

    public runWorkflow(workflowId: string, workflowName: string): void {
        this.store.dispatch(new workflowActions.RunWorkflowAction({
            id: workflowId,
            name: workflowName
        }));
    }

    public stopWorkflow(workflowStatus: any): void {
        this.store.dispatch(new workflowActions.StopWorkflowAction(workflowStatus));
    }

    public onCloseJsonModal(action: any) {
        this._modalService.close();
    }

    public getTableFields(): StTableHeader[] {
        return [
            { id: 'isChecked', label: '', sortable: false },
            { id: 'name', label: 'Name' },
            { id: 'executionEngine', label: 'type'},
            { id: 'context.status', label: 'Status' },
            { id: 'spark', label: '', sortable: false }
        ];
    }


    constructor(private store: Store<fromRoot.State>, private _modalService: StModalService, private translate: TranslateService,
        private route: Router, private currentActivatedRoute: ActivatedRoute) {
        const workflowModalCt = 'DASHBOARD.NEW_WORKFLOW';
        const workflowModalTitle = 'DASHBOARD.CHOOSE_METHOD';
        const workflowJsonModalTitle = 'DASHBOARD.JSON_TITLE';
        const deleteWorkflowModalTitle = 'DASHBOARD.DELETE_WORKFLOW_TITLE';
        const deleteWorkflowModalMessage = 'DASHBOARD.DELETE_WORKFLOW_MESSAGE';
        const messageDeleteTitle = 'DASHBOARD.MESSAGE_DELETE_TITLE';
        this.translate.get([workflowModalCt, workflowModalTitle, workflowJsonModalTitle,
            deleteWorkflowModalTitle, deleteWorkflowModalMessage, messageDeleteTitle]).subscribe(
            (value: { [key: string]: string }) => {
                this.workflowModalCt = value[workflowModalCt].toUpperCase();
                this.workflowModalTitle = value[workflowModalTitle];
                this.workflowJsonModalTitle = value[workflowJsonModalTitle];
                this.deleteWorkflowModalTitle = value[deleteWorkflowModalTitle];
                this.deleteWorkflowModalMessage = value[deleteWorkflowModalMessage];
                this.messageDeleteTitle = value[messageDeleteTitle];
            }
            );
    }
}
