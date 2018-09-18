/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './layout.component';

const layoutRoutes: Routes = [
    {
        path: '',
        component: LayoutComponent,
        children: [
            {
               path: '',
               loadChildren: '@app/executions/executions-monitoring/executions.monitoring.module#ExecutionsMonitoringModule'
            },
            {
               path: 'executions',
               loadChildren: '@app/executions/executions-managing/executions.managing.module#ExecutionsManagingModule'
           },
            {
                path: 'repository',
                loadChildren: '@app/workflows/workflow-managing/workflows.module#WorkflowsManageModule'
            },
            {
                path: 'templates',
                loadChildren: '@app/templates/templates.module#TemplatesModule'
            },
            {
                path: 'sparta-settings',
                loadChildren: '@app/settings/settings.module#SettingsModule'
            },
            {
                path: 'catalog',
                loadChildren: '@app/crossdata/crossdata.module#CrossdataModule'
            }
        ]
    }
];

@NgModule({
    exports: [
        RouterModule
    ],
    imports: [
        RouterModule.forChild(layoutRoutes)
    ]
})

export class LayoutRouter { }
