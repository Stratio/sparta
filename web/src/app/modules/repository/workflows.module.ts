
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { CommonModule } from '@angular/common';
import { EffectsModule } from '@ngrx/effects';
import { StModalService, StProgressBarModule, StModalModule } from '@stratio/egeo';

import { WorkflowsManagingService } from './workflows.service';
import { WorkflowManagingRouterModule } from './workflows.router';
import { WorkflowsManagingComponent } from './workflows.component';

import { RepositoryTableHeaderModule } from './components/workflow-table-header/workflow-table-header.module';
import { RepositoryTableModule } from './components/repository-table/repository-table.module';
import { RepositoryHeaderModule } from './components/repository-header/repository-header.module';
import { RepositoryDetailModule } from './components/repository-detail/repository-detail.module';

import { reducers } from './reducers';
import { WorkflowEffect } from './effects/workflow';
import { CustomExecutionModule } from '@app/custom-execution/custom-execution.module';
import { SpartaSidebarModule } from '@app/shared';

import { WorkflowGroupModalModule } from './components/workflow-group-modal/workflow-group-modal.module';
import { WorkflowGroupModalComponent } from './components/workflow-group-modal/workflow-group-modal.component';
import { WorkflowJsonModalComponent } from './components/workflow-json-modal/workflow-json-modal.component';
import { WorkflowJsonModalModule } from './components/workflow-json-modal/workflow-json-modal.module';
import { WorkflowSchedulerComponent } from '@app/shared/components/workflow-scheduler/workflow-scheduler.component';
import { WorkflowSchedulerModule } from '@app/shared/components/workflow-scheduler/workflow-scheduler-modal.module';

@NgModule({
    declarations: [
        WorkflowsManagingComponent
    ],
    imports: [
        CommonModule,
        StoreModule.forFeature('workflowsManaging', reducers),
        EffectsModule.forFeature([WorkflowEffect]),
        CustomExecutionModule,
        SpartaSidebarModule,
        StProgressBarModule,
        StModalModule.withComponents([WorkflowGroupModalComponent, WorkflowJsonModalComponent, WorkflowSchedulerComponent]),
        WorkflowManagingRouterModule,
        RepositoryDetailModule,
        RepositoryHeaderModule,
        RepositoryTableHeaderModule,
        RepositoryTableModule,
        WorkflowGroupModalModule,
        WorkflowSchedulerModule,
        WorkflowJsonModalModule,
    ],
    providers: [
        WorkflowsManagingService,
        StModalService
    ]
})

export class WorkflowsManageModule { }
