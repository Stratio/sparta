/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { FormsModule } from '@angular/forms';
import { EffectsModule } from '@ngrx/effects';
import { StBreadcrumbsModule, StModalModule, StTableModule, StCheckboxModule, StHorizontalTabsModule,
    StModalService, StProgressBarModule, StTextareaModule } from '@stratio/egeo';
import { PerfectScrollbarModule } from 'ngx-perfect-scrollbar';

import { SharedModule } from '@app/shared';
import {
    WorkflowsManagingComponent, WorkflowsManagingService, WorkflowCreationModal,
    WorkflowJsonModal,  WorkflowManagingRouterModule, WorkflowManagingDetailComponent,
    WorkflowGroupModal, WorkflowsManagingHeaderContainer, WorkflowsManagingHeaderComponent,
    WorkflowsManagingTableComponent, WorkflowsManagingTableContainer, WorkflowRenameModal,
    MoveGroupModal, GroupTreeComponent, DuplicateWorkflowComponent, GroupSelectorComponent
} from '.';

import { reducers } from './reducers';
import { WorkflowEffect } from './effects/workflow';
import { SpTooltipModule } from '@app/shared/components/sp-tooltip/sp-tooltip.module';
;
@NgModule({
    exports: [
      WorkflowJsonModal
   ],
    declarations: [
        WorkflowsManagingComponent,
        WorkflowCreationModal,
        WorkflowManagingDetailComponent,
        WorkflowJsonModal,
        WorkflowGroupModal,
        WorkflowsManagingHeaderContainer,
        WorkflowsManagingHeaderComponent,
        WorkflowsManagingTableComponent,
        WorkflowsManagingTableContainer,
        WorkflowRenameModal,
        GroupSelectorComponent,
        GroupTreeComponent,
        DuplicateWorkflowComponent,
        MoveGroupModal
    ],
    imports: [
        FormsModule,
        StoreModule.forFeature('workflowsManaging', reducers),
        EffectsModule.forFeature([WorkflowEffect]),
        PerfectScrollbarModule,
        SpTooltipModule,
        StCheckboxModule,
        StHorizontalTabsModule,
        StProgressBarModule,
        StTableModule,
        StTextareaModule,
        StBreadcrumbsModule,
        WorkflowManagingRouterModule,
        SharedModule,
        StModalModule.withComponents([WorkflowCreationModal, WorkflowJsonModal,
        WorkflowGroupModal, WorkflowRenameModal, MoveGroupModal, GroupTreeComponent,
        DuplicateWorkflowComponent])
    ],
    providers: [
        WorkflowsManagingService,
        StModalService
    ]
})

export class WorkflowsManageModule {
}
