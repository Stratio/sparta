///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';
import { EgeoModule, StModalModule, StSwitchModule, StTableModule } from '@stratio/egeo';
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
        EgeoModule.forRoot(),
        StModalModule.withComponents([ExecuteBackup]),
        BackupsRoutingModule,
        EffectsModule.forFeature([BackupsEffect]),
        StoreModule.forFeature('backups', reducers),
        FormsModule,
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

