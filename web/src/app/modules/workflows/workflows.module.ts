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
import { FormsModule } from '@angular/forms';
import {
    WorkflowsComponent, WorkflowsService, WorkflowCreationModal, WorkflowJsonModal,  WorkflowRoutingModule, WorkflowDetailComponent,
    WorkflowExecutionInfoComponent
} from '.';
import { SharedModule } from '@app/shared';
import { StBreadcrumbsModule, StModalModule, StTableModule, StCheckboxModule, StHorizontalTabsModule } from '@stratio/egeo';

@NgModule({
    declarations: [
        WorkflowsComponent,
        WorkflowCreationModal,
        WorkflowDetailComponent,
        WorkflowJsonModal,
        WorkflowExecutionInfoComponent
    ],
    imports: [
        FormsModule,
        StCheckboxModule,
        StHorizontalTabsModule,
        StTableModule,
        StBreadcrumbsModule,
        StModalModule.withComponents([WorkflowCreationModal, WorkflowJsonModal, WorkflowExecutionInfoComponent]),
        WorkflowRoutingModule,
        SharedModule
    ],
    providers: [
        WorkflowsService
    ]
})

export class WorkflowsModule {
}
