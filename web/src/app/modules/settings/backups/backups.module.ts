/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';
import { StModalModule, StSwitchModule, StTableModule, StBreadcrumbsModule, StCheckboxModule } from '@stratio/egeo';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { FormsModule } from '@angular/forms';

import { SpartaBackups } from './backups.component';
import { BackupsRoutingModule } from './backups.routes';
import { ExecuteBackup } from './components/execute-backup/execute-backup.component';
import { BackupsEffect } from './effects/backups';
import { reducers } from './reducers';
import { TableNotificationModule } from '@app/shared/components/table-notification/table-notification.module';

@NgModule({
   declarations: [
      SpartaBackups,
      ExecuteBackup,
   ],
   imports: [
      BackupsRoutingModule,
      StoreModule.forFeature('backups', reducers),
      EffectsModule.forFeature([BackupsEffect]),
      FormsModule,
      StBreadcrumbsModule,
      StCheckboxModule,
      StModalModule.withComponents([ExecuteBackup]),
      StSwitchModule,
      SharedModule,
      TableNotificationModule,
      StTableModule
   ]
})

export class BackupsModule {
   constructor() {

   }
}

