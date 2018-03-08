/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EgeoModule, StModalModule } from '@stratio/egeo';

import { SettingsRoutingModule } from './settings-routing.module';
import { SpartaPlugins } from './resources/plugins/plugins.component';
import { SpartaDrivers } from './resources/drivers/drivers.component';
import { SpTooltipModule } from '@app/shared/components/sp-tooltip/sp-tooltip.module';
import { EnvironmentModule } from './environment/environment.module';
import { SettingsComponent } from './settings.component';
import { SharedModule } from '@app/shared';
import { BackupsModule } from './backups/backups.module';
import { TableNotificationModule } from '@app/shared/components/table-notification/table-notification.module';

@NgModule({
    declarations: [
        SettingsComponent,
        SpartaDrivers,
        SpartaPlugins,
    ],
    imports: [
        EgeoModule.forRoot(),
        FormsModule,
        ReactiveFormsModule,
        EnvironmentModule,
        BackupsModule,
        SpTooltipModule,
        SettingsRoutingModule,
        SharedModule,
        TableNotificationModule
    ]
})

export class SettingsModule {
    constructor() {

    }
}

