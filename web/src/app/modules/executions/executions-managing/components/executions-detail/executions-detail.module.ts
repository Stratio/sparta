/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ExecutionsDetailComponent } from './executions-detail.component';
import { GraphEditorModule, SpartaSidebarModule } from '@app/shared';
import { WizardNodeModule } from '@app/wizard/components/wizard-node/wizard-node.module';
import { EdgeOptionsModule } from '@app/wizard/components/edge-options/edge-options.module';
import { WizardEdgeModule } from '@app/wizard/components/wizard-edge/wizard-edge.module';
import { Routes, RouterModule } from '@angular/router';

const executionDetailRoutes: Routes = [
  {
    path: '',
    component: ExecutionsDetailComponent
  }
];

@NgModule({
  exports: [
    RouterModule
  ],
  imports: [
    RouterModule.forChild(executionDetailRoutes)
  ]
})

export class ExecutionDetailsRouterModule { }


@NgModule({
  imports: [
    CommonModule,
    ExecutionDetailsRouterModule,
    GraphEditorModule,
    WizardNodeModule,
    EdgeOptionsModule,
    WizardEdgeModule,
    RouterModule,
    SpartaSidebarModule,
    TranslateModule
  ],
  declarations: [ExecutionsDetailComponent],
  exports: [ExecutionsDetailComponent]
})
export class ExecutionsDetailModule { }
