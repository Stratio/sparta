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

import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { FormsModule } from '@angular/forms';
import { EffectsModule } from '@ngrx/effects';
import { StBreadcrumbsModule, StModalModule, StTableModule, StCheckboxModule, StHorizontalTabsModule, StModalService } from '@stratio/egeo';

import { SharedModule } from '@app/shared';
import {
    WorkflowsComponent, WorkflowsService, WorkflowCreationModal, WorkflowJsonModal,  WorkflowRouterModule, WorkflowDetailComponent,
    WorkflowExecutionInfoComponent, WorkflowGroupModal, WorkflowsHeaderContainer, WorkflowsHeaderComponent,
    WorkflowsTableComponent, WorkflowsTableContainer
} from '.';

import { reducers } from './reducers';
import { WorkflowEffect } from './effects/workflow';
;
@NgModule({
    declarations: [
        WorkflowsComponent,
        WorkflowCreationModal,
        WorkflowDetailComponent,
        WorkflowJsonModal,
        WorkflowGroupModal,
        WorkflowExecutionInfoComponent,
        WorkflowsHeaderContainer,
        WorkflowsHeaderComponent,
        WorkflowsTableComponent,
        WorkflowsTableContainer
    ],
    imports: [
        FormsModule,
        StoreModule.forFeature('workflows', reducers),
        EffectsModule.forFeature([WorkflowEffect]),
        StCheckboxModule,
        StHorizontalTabsModule,
        StTableModule,
        StBreadcrumbsModule,
        WorkflowRouterModule,
        SharedModule,
        StModalModule.withComponents([WorkflowCreationModal, WorkflowJsonModal, WorkflowExecutionInfoComponent, WorkflowGroupModal])
    ],
    providers: [
        WorkflowsService,
        StModalService
    ]
})

export class WorkflowsModule {
}
