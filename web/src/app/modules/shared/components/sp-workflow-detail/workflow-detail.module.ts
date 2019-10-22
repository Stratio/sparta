/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { GraphEditorModule, SpartaSidebarModule } from '@app/shared';
import { WizardNodeModule } from '@app/wizard/components/wizard-node/wizard-node.module';
import { EdgeOptionsModule } from '@app/wizard/components/edge-options/edge-options.module';
import { WizardEdgeModule } from '@app/wizard/components/wizard-edge/wizard-edge.module';
import { RouterModule } from '@angular/router';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import { WorkflowDetailExecutionEffect } from '@app/executions/workflow-detail-execution/effects/workflow-detail-execution';
import { workflowDetailExecutionReducers } from '@app/executions/workflow-detail-execution/reducers';
import { SchemaDataModule } from '@app/shared/components/schema-data/schema-data.module';
import { StHorizontalTabsModule } from '@stratio/egeo';
import { DropDownTitleModule } from '@app/shared/components/drop-down-title/drop-down-title.module';
import { ExecutionHelperService } from 'app/services/helpers/execution.service';
import { QualityRulesModule} from '@app/shared/components/quality-rules/quality-rules.module';
import { WorkflowViewerConfigModule } from './components/workflow-detail-modal/workflow-detail-modal.module';
import { WorkflowDetailSidebarModule } from './components/workflow-detail-sidebar/workflow-detail-sidebar.module';
import {WorkflowDetailComponent} from '@app/shared/components/sp-workflow-detail/workflow-detail.component';
import {WorkflowDetailRouterModule} from '@app/shared/components/sp-workflow-detail/workflow-detail.router';
@NgModule({
  imports: [
    CommonModule,
    WorkflowDetailRouterModule,
    GraphEditorModule,
    WizardNodeModule,
    EdgeOptionsModule,
    WizardEdgeModule,
    RouterModule,
    SpartaSidebarModule,
    StHorizontalTabsModule,
    SchemaDataModule,
    TranslateModule,
    DropDownTitleModule,
    QualityRulesModule,
    WorkflowViewerConfigModule,
    WorkflowDetailSidebarModule,
    StoreModule.forFeature('workflowDetail', workflowDetailExecutionReducers),
    EffectsModule.forFeature([WorkflowDetailExecutionEffect]),
  ],
  declarations: [WorkflowDetailComponent],
  exports: [WorkflowDetailComponent],
  providers: [ExecutionHelperService]
})

export class WorkflowDetailModule { }
