/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Error404Component } from '@app/errors';

const appRoutes: Routes = [
    {
        path: '',
        loadChildren: './modules/layout/layout.module#LayoutModule'
    },
    {
        path: 'wizard',
        loadChildren: './modules/wizard/wizard.module#WizardModule'
    },
    {
        path: '**',
        component: Error404Component
    }
];

@NgModule({
    exports: [
        RouterModule
    ],
    imports: [
        RouterModule.forRoot(appRoutes, { enableTracing: false, useHash: true })
    ]
})

export class AppRouter { }
