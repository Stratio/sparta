/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StBreadcrumbsModule } from '@stratio/egeo';

import { ToolBarModule } from '@app/shared';

import { ExecutionsToolbarComponent } from './executions-toolbar.component';

@NgModule({
   imports: [
     CommonModule,
     StBreadcrumbsModule,
     ToolBarModule
   ],
   declarations: [ExecutionsToolbarComponent],
   exports: [ExecutionsToolbarComponent]
})
export class ExecutionsToolbarModule { }
