/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { StFullscreenLayoutModule, StHorizontalTabsModule } from '@stratio/egeo';

import { SchemaDataModule } from '@app/shared/components/schema-data/schema-data.module';
import { SpAccordionModule } from '@app/shared/components/sp-accordion/sp-accordion.module';
import { WorkflowDetailModalComponent } from './workflow-detail-modal.component';

@NgModule({
  exports: [
    WorkflowDetailModalComponent
  ],
  declarations: [
    WorkflowDetailModalComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    StFullscreenLayoutModule,
    StHorizontalTabsModule,
    SpAccordionModule,
    TranslateModule,
    SchemaDataModule
  ]
})

export class WorkflowViewerConfigModule { }
