/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ExecutionsComponent } from './executions.component';

import { RouterModule } from '@angular/router';
import { ExecutionsRouterModule } from './executions.router';
import { StHorizontalTabsModule } from '@stratio/egeo';
import { ExecutionsListModule } from './executions-list/executions-list.module';
import { ScheduledModule } from './executions-scheduled/scheduled.module';
import { ExecutionsToolbarModule } from './executions-toolbar/executions-toolbar.module';
import { SpFooterModule } from '@app/shared/components/sp-footer/sp-footer.module';


@NgModule({
   declarations: [
      ExecutionsComponent
   ],
   imports: [
      CommonModule,
      ExecutionsRouterModule,
      ExecutionsListModule,
      ExecutionsToolbarModule,
      ScheduledModule,
      RouterModule,
      SpFooterModule,
      StHorizontalTabsModule
   ]
})

export class ExecutionsManagingModule { }
