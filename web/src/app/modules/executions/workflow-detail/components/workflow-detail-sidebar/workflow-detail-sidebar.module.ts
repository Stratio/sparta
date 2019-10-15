/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { StHorizontalTabsModule } from '@stratio/egeo';
import { SpartaSidebarModule } from '@app/shared';
import { SchemaDataModule } from '@app/shared/components/schema-data/schema-data.module';
import { DropDownTitleModule } from '@app/shared/components/drop-down-title/drop-down-title.module';
import { QualityRulesModule } from '@app/shared/components/quality-rules/quality-rules.module';
import { WorkflowDetailSidebarComponent } from './workflow-detail-sidebar.component';


@NgModule({
  imports: [
    CommonModule,
    SpartaSidebarModule,
    StHorizontalTabsModule,
    SchemaDataModule,
    TranslateModule,
    DropDownTitleModule,
    QualityRulesModule,
  ],
  declarations: [WorkflowDetailSidebarComponent],
  exports: [WorkflowDetailSidebarComponent]
})

export class WorkflowDetailSidebarModule { }
