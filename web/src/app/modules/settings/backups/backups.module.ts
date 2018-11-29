/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StModalModule, StModalService, StSwitchModule, StTableModule, StBreadcrumbsModule, StCheckboxModule } from '@stratio/egeo';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { FormFileModule, TableNotificationModule, ToolBarModule } from '@app/shared';

import { SpartaBackups } from './backups.component';
import { BackupsRoutingModule } from './backups.routes';
import { ExecuteBackup } from './components/execute-backup/execute-backup.component';
import { BackupsEffect } from './effects/backups';
import { reducers } from './reducers';

@NgModule({
   declarations: [
      SpartaBackups,
      ExecuteBackup
   ],
   imports: [
      CommonModule,
      FormFileModule,
      BackupsRoutingModule,
      StoreModule.forFeature('backups', reducers),
      EffectsModule.forFeature([BackupsEffect]),
      FormsModule,
      StBreadcrumbsModule,
      StCheckboxModule,
      StModalModule.withComponents([ExecuteBackup]),
      StSwitchModule,
      TableNotificationModule,
      StTableModule,
      ToolBarModule,
      TranslateModule
   ],
   providers: [
      StModalService
   ]
})

export class BackupsModule {
   constructor() {

   }
}

