/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EffectsModule } from '@ngrx/effects';
import { StBreadcrumbsModule, StModalModule, StCheckboxModule, StHorizontalTabsModule,
    StModalService, StProgressBarModule, StTextareaModule, StInputModule
} from '@stratio/egeo';
import { PerfectScrollbarModule } from 'ngx-perfect-scrollbar';

import { SharedModule } from '@app/shared';
import {
    WorkflowsManagingComponent, WorkflowsManagingService, WorkflowCreationModal,
    WorkflowJsonModal,  WorkflowManagingRouterModule,
    WorkflowGroupModal, WorkflowRenameModalComponent,
    MoveGroupModal, GroupTreeComponent, DuplicateWorkflowComponent, GroupSelectorComponent
} from '.';

import { RepositoryTableHeaderModule } from './components/workflow-table-header/workflow-table-header.module';
import { RepositoryTableModule } from './components/repository-table/repository-table.module';
import { RepositoryHeaderModule } from './components/repository-header/repository-header.module';
import { RepositoryDetailModule } from './components/repository-detail/repository-detail.module';

import { reducers } from './reducers';
import { WorkflowEffect } from './effects/workflow';
import { SpSelectModule } from '@app/shared/components/sp-select/sp-select.module';
import { CustomExecutionModule } from '@app/custom-execution/custom-execution.module';

@NgModule({
    exports: [
      WorkflowJsonModal
   ],
    declarations: [
        WorkflowsManagingComponent,
        WorkflowCreationModal,
        WorkflowJsonModal,
        WorkflowGroupModal,
        WorkflowRenameModalComponent,
        GroupSelectorComponent,
        GroupTreeComponent,
        DuplicateWorkflowComponent,
        MoveGroupModal
    ],
    imports: [
        FormsModule,
        ReactiveFormsModule,
        StoreModule.forFeature('workflowsManaging', reducers),
        EffectsModule.forFeature([WorkflowEffect]),
        PerfectScrollbarModule,
        CustomExecutionModule,
        StHorizontalTabsModule,
        StProgressBarModule,
        StTextareaModule,
        SpSelectModule,
        StInputModule,
        WorkflowManagingRouterModule,
        RepositoryDetailModule,
        RepositoryHeaderModule,
        RepositoryTableHeaderModule,
        RepositoryTableModule,
        SharedModule,
        StModalModule.withComponents([WorkflowCreationModal, WorkflowJsonModal,
        WorkflowGroupModal, WorkflowRenameModalComponent, MoveGroupModal, GroupTreeComponent,
        DuplicateWorkflowComponent])
    ],
    providers: [
        WorkflowsManagingService,
        StModalService
    ]
})

export class WorkflowsManageModule { }
