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
import {workflowDetailRepoReducers} from '@app/repository/components/workflow-detail-repo/reducers';
import { SchemaDataModule } from '@app/shared/components/schema-data/schema-data.module';
import { StHorizontalTabsModule } from '@stratio/egeo';
import { DropDownTitleModule } from '@app/shared/components/drop-down-title/drop-down-title.module';
import { ExecutionHelperService } from 'app/services/helpers/execution.service';
import { QualityRulesModule} from '@app/shared/components/quality-rules/quality-rules.module';
import {WorkflowDetailModule} from '@app/shared/components/sp-workflow-detail/workflow-detail.module';
import {WorkflowDetailRepoContainer} from '@app/repository/components/workflow-detail-repo/workflow-detail-repo.container';
import {WorkflowDetailRepoEffect} from '@app/repository/components/workflow-detail-repo/effects/workflow-detail-repo';
import {WorkflowDetailRepoRouter} from '@app/repository/components/workflow-detail-repo/workflow-detail-repo.router';

@NgModule({
  imports: [
    CommonModule,
    GraphEditorModule,
    WorkflowDetailRepoRouter,
    WizardNodeModule,
    EdgeOptionsModule,
    WizardEdgeModule,
    RouterModule,
    SpartaSidebarModule,
    StHorizontalTabsModule,
    WorkflowDetailModule,
    SchemaDataModule,
    TranslateModule,
    DropDownTitleModule,
    QualityRulesModule,
    StoreModule.forFeature('workflowDetailRepo', workflowDetailRepoReducers),
    EffectsModule.forFeature([WorkflowDetailRepoEffect]),
  ],
  declarations: [WorkflowDetailRepoContainer],
  exports: [WorkflowDetailRepoContainer],
  providers: [ExecutionHelperService]
})

export class WorkflowDetailRepoModule { }
