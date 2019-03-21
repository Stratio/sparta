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
import { WorkflowDetailRouterModule } from '@app/executions/workflow-detail/workflow-detail.router';
import { WorkflowDetailComponent } from '@app/executions/workflow-detail/workflow-detail.component';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {WorkflowDetailEffect} from '@app/executions/workflow-detail/effects/workflow-detail';
import {workflowDetailReducers} from '@app/executions/workflow-detail/reducers';
import { SchemaDataModule } from '@app/shared/components/schema-data/schema-data.module';
import { StHorizontalTabsModule } from '@stratio/egeo';

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
    StoreModule.forFeature('workflowDetail', workflowDetailReducers),
    EffectsModule.forFeature([WorkflowDetailEffect]),
  ],
  declarations: [WorkflowDetailComponent],
  exports: [WorkflowDetailComponent]
})

export class WorkflowDetailModule { }
