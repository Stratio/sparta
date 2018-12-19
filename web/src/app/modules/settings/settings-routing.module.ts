/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { SettingsComponent } from './settings.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SettingsHomeComponent } from './settings/settings.component';


const settingsRoutes: Routes = [
    {
        path: '',
        component: SettingsComponent,
        children: [
            {
                path: '',
                component: SettingsHomeComponent
            },
            {
                path: 'plugins',
                loadChildren: './plugins/plugins.module#PluginsModule'
            },
            {
                path: 'parameter-group',
                loadChildren: './parameter-group/parameter-group.module#ParameterGroupModule'
            }
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
