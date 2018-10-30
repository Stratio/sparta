/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

import { StBreadcrumbsModule, StForegroundNotificationsModule } from '@stratio/egeo';

import { RepositoryHeaderContainer } from './repository-header.container';
import { RepositoryHeaderComponent } from './repository-header.component';

import { MenuOptionsListModule } from '@app/shared/components/menu-options-list/menu-options-list.module';
import { ToolBarModule } from '@app/shared/components/tool-bar/tool-bar.module';
import { SpTitleModule } from '@app/shared/components/sp-title/sp-title.module';

@NgModule({
   exports: [
      RepositoryHeaderContainer
   ],
    declarations: [
      RepositoryHeaderComponent,
      RepositoryHeaderComponent,
      RepositoryHeaderContainer
    ],
    imports: [
       CommonModule,
       MenuOptionsListModule,
       StBreadcrumbsModule,
       StForegroundNotificationsModule,
       SpTitleModule,
       TranslateModule,
       ToolBarModule
    ]
})

export class RepositoryHeaderModule { }
