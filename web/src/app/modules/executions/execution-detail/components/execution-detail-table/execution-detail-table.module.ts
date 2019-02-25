/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StTableModule, StCheckboxModule, StSearchModule, StDropdownMenuModule } from '@stratio/egeo';

import { ExecutionDetailTableComponent } from './execution-detail-table.component';
import { ExecutionDetailTableContainer } from './execution-detail-table.container';
import { ExecutionDetailDropDownTitleModule } from './execution-detail-drop-down-title/execution-detail-drop-down-title.module';

@NgModule({
  imports: [
    CommonModule,
    StTableModule,
    StCheckboxModule,
    StSearchModule,
    ExecutionDetailDropDownTitleModule,
    StDropdownMenuModule
  ],
  declarations: [ ExecutionDetailTableComponent, ExecutionDetailTableContainer ],
  exports: [ ExecutionDetailTableContainer ],
  providers: [],
})
export class ExecutionDetailTableModule {}
