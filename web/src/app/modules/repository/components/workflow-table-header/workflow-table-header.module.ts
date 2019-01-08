/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

import { StSearchModule } from '@stratio/egeo';

import { MenuOptionsListModule, SpTitleModule } from '@app/shared';

import { WorkflowTableHeaderComponent } from './workflow-table-header.component';
import { WorkflowTableHeaderContainer } from './workflow-table-header.container';
import { WorkflowBreadcrumbComponent } from './workflow-breadcrumb/workflow-breadcrumb.component';

@NgModule({
   exports: [
      WorkflowTableHeaderContainer
   ],
    declarations: [
      WorkflowBreadcrumbComponent,
      WorkflowTableHeaderComponent,
      WorkflowTableHeaderContainer
    ],
    imports: [
       CommonModule,
       MenuOptionsListModule,
       StSearchModule,
       SpTitleModule,
       TranslateModule,
    ]
})

export class RepositoryTableHeaderModule {
}
