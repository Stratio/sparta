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
import { SettingsComponent } from './settings.component';
import { SettingsRoutingModule } from './settings-routing.module';
import { SharedModule } from '@app/shared';
import { EgeoModule, StModalModule } from '@stratio/egeo';
import { SpartaBackups } from './backups/backups.component';
import { SpartaPlugins } from './resources/plugins/plugins.component';
import { SpartaDrivers } from './resources/drivers/drivers.component';
import { ExecuteBackup } from './backups/execute-backup/execute-backup.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { EnvironmentComponent } from './environment/environment.component';
import { ImportEnvironmentModalComponent } from './environment/import-environment-modal/import-environment-modal.component';
import { SpTooltipModule } from '@app/shared/components/sp-tooltip/sp-tooltip.module';
import { EnvironmentModule } from '@app/settings/environment/environment.module';

@NgModule({
    declarations: [
        SettingsComponent,
        SpartaBackups,
        ExecuteBackup,
        SpartaDrivers,
        SpartaPlugins,
    ],
    imports: [
        EgeoModule.forRoot(),
        FormsModule,
        ReactiveFormsModule,
        EnvironmentModule,
        SpTooltipModule,
        StModalModule.withComponents([ExecuteBackup]),
        SettingsRoutingModule,
        SharedModule
    ]
})

export class SettingsModule {
    constructor() {

    }
}

