/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { StModalService } from '@stratio/egeo';
import { ActivatedRoute, Router } from '@angular/router';

import { Subscription } from 'rxjs';

import { WorkflowGroupModalComponent } from './components/workflow-group-modal/workflow-group-modal.component';
import { WorkflowJsonModalComponent } from './components/workflow-json-modal/workflow-json-modal.component';
import * as fromRoot from 'reducers';
import * as workflowActions from './actions/workflow-list';
import { WorkflowSchedulerComponent } from '@app/shared/components/workflow-scheduler/workflow-scheduler.component';

@Injectable()
export class WorkflowsManagingService {

  public workflowModalTitle: string;
  public workflowModalCt: string;
  public workflowJsonModalTitle: string;
  public workflowScheduleModalTitle: string;

  public modalSubscription: Subscription;
  public createGroupModalTitle: string;

  public showCreateJsonModal(): void {
    this._modalService.show({
      modalTitle: this.workflowJsonModalTitle,
      maxWidth: 980,
      outputs: {
        onCloseJsonModal: this.onCloseJsonModal.bind(this)
      },
    }, WorkflowJsonModalComponent);
  }

  public runWorkflow(versionId: string, workflowName: string): void {
    this.store.dispatch(new workflowActions.RunWorkflowAction({
      id: versionId,
      name: workflowName
    }));
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
    }, WorkflowGroupModalComponent);
  }

  public createSchedule(versionId: string): void {
    this._modalService.show({
      modalTitle: this.workflowScheduleModalTitle,
      maxWidth: 500,
      outputs: {
        onCloseScheduleModal: (data) => {
          if(data) {
            data.entityId = versionId;
            this.store.dispatch(new workflowActions.CreateScheduledExecution(data));
          } 
          this._modalService.close()
        }
      },
    }, WorkflowSchedulerComponent);
  }

  constructor(private store: Store<fromRoot.State>, private _modalService: StModalService, private translate: TranslateService,
    private route: Router, private currentActivatedRoute: ActivatedRoute) {
    const workflowModalCt = 'DASHBOARD.NEW_WORKFLOW';
    const createGroupModalTitle = 'DASHBOARD.CREATE_GROUP_TITLE';
    const workflowModalTitle = 'DASHBOARD.CHOOSE_METHOD';
    const workflowJsonModalTitle = 'DASHBOARD.JSON_TITLE';
    const workflowScheduleModalTitle = 'DASHBOARD.CREATE_SCHEDULE_TITLE';

    const titles = [workflowModalCt, workflowModalTitle, workflowJsonModalTitle,
      createGroupModalTitle, workflowScheduleModalTitle];

    this.translate.get(titles).subscribe(
      (value: { [key: string]: string }) => {
        this.workflowModalCt = value[workflowModalCt].toUpperCase();
        this.workflowModalTitle = value[workflowModalTitle];
        this.workflowJsonModalTitle = value[workflowJsonModalTitle];
        this.createGroupModalTitle = value[createGroupModalTitle];
        this.workflowScheduleModalTitle = value[workflowScheduleModalTitle];
      });
  }
}
