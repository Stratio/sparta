/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { SettingsComponent } from './settings.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SpartaBackups } from './backups/backups.component';
import { SpartaDrivers } from './resources/drivers/drivers.component';
import { SpartaPlugins } from './resources/plugins/plugins.component';
import { EnvironmentComponent } from './environment/environment.component';


const settingsRoutes: Routes = [
    {
        path: '',
        component: SettingsComponent,
        children: [
            {
                path: '',
                redirectTo: 'backups'
            },
            {
                path: 'backups',
                loadChildren: './backups/backups.module#BackupsModule'
            },
            {
                path: 'resources',
                redirectTo: 'resources/plugins'
            },
            {
                path: 'resources/drivers',
                component: SpartaDrivers
            },
            {
                path: 'plugins',
                component: SpartaPlugins
            },
            {
                path: 'environment',
                loadChildren: './environment/environment.module#EnvironmentModule'
            },
        ]
    }
];

@NgModule({
    exports: [
        RouterModule
    ],
    imports: [
        RouterModule.forChild(settingsRoutes)
    ]
})

export class SettingsRoutingModule { }
