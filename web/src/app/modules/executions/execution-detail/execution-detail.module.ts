/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { EffectsModule } from '@ngrx/effects';
import { reducers } from './reducers';

import { ExecutionDetailEffect } from './effects/execution-detail';

import { ExecutionDetailComponent } from './execution-detail.component';
import { ExecutionDetailRouterModule } from './execution-detail.router';
import { ExecutionDetailTableModule } from './components/execution-detail-table/execution-detail-table.module';
import { DetailInfoModule } from '@app/executions/execution-detail/components/execution-detail-info/detail-info.module';
import { StBreadcrumbsModule, StModalService, StModalModule } from '@stratio/egeo';
import { ToolBarModule } from '@app/shared';
import { WorkflowDetailModule } from '@app/executions/workflow-detail/workflow-detail.module';
import { ExecutionDetailHelperService } from '@app/executions/execution-detail/services/execution-detail.service';
import { QualityRulesModule } from '@app/shared/components/quality-rules/quality-rules.module';
import { ModalLayoutModule } from '@app/shared/components/modal-layout/modal-layout.module';
import { ExecutionsConsoleModule } from '../executions-managing/executions-list/components/executions-console/executions-console.module';


@NgModule({
    declarations: [
      ExecutionDetailComponent
    ],
    imports: [
        CommonModule,
        StoreModule.forFeature('executionDetail', reducers),
        EffectsModule.forFeature([ExecutionDetailEffect]),
        ExecutionsConsoleModule,
        ExecutionDetailRouterModule,
        QualityRulesModule,
        StBreadcrumbsModule,
        TranslateModule,
        ExecutionDetailTableModule,
        WorkflowDetailModule,
        DetailInfoModule,
        ToolBarModule,
        StModalModule,
        ModalLayoutModule
    ],
    providers: [ExecutionDetailHelperService, StModalService]
})

export class ExecutionDetailModule {
}
