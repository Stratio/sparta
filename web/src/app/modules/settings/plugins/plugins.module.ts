/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';
import { EgeoModule, StModalModule, StSwitchModule, StTableModule, StBreadcrumbsModule, StCheckboxModule } from '@stratio/egeo';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { FormsModule } from '@angular/forms';

import { PluginsEffect } from './effects/plugins';
import { reducers } from './reducers';
import { PluginsComponent } from './plugins.component';
import { TableNotificationModule } from '@app/shared/components/table-notification/table-notification.module';
import { PluginRoutingModule } from './plugins.routes';

@NgModule({
    declarations: [
        PluginsComponent
    ],
    imports: [
        StoreModule.forFeature('plugins', reducers),
        EffectsModule.forFeature([PluginsEffect]),
        FormsModule,
        StSwitchModule,
        PluginRoutingModule,
        StBreadcrumbsModule,
        TableNotificationModule,
        StCheckboxModule,
        StTableModule,
        SharedModule,
        StTableModule
    ]
})

export class PluginsModule {
    constructor() {

    }
}

