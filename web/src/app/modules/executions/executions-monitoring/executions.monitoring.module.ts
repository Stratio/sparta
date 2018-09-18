/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { FormsModule } from '@angular/forms';
import { EffectsModule } from '@ngrx/effects';
import { StBreadcrumbsModule, StTableModule,
    StSearchModule, StCheckboxModule, StHorizontalTabsModule,
    StPaginationModule, StTooltipModule, StFullscreenLayoutModule
} from '@stratio/egeo';

import { SharedModule } from '@app/shared';
import {
   ExecutionsComponent,
   ExecutionsRouterModule,
   ExecutionsHeaderComponent,
   ExecutionsTableComponent
} from '.';

import { reducerToken, reducerProvider } from './reducers';
import { ExecutionsEffect } from './effects/executions';
import { SpTooltipModule } from '@app/shared/components/sp-tooltip/sp-tooltip.module';
import { ConsoleBoxModule } from '@app/shared/components/console-box/console.box.module';


@NgModule({
    declarations: [
      ExecutionsComponent,
      ExecutionsHeaderComponent,
      ExecutionsTableComponent
    ],
    imports: [
        FormsModule,
        SpTooltipModule,
        StoreModule.forFeature('executionsMonitoring', reducerToken),
        EffectsModule.forFeature([ExecutionsEffect]),
        StCheckboxModule,
        StHorizontalTabsModule,
        StFullscreenLayoutModule,
        StTableModule,
        StTooltipModule,
        StBreadcrumbsModule,
        StSearchModule,
        ExecutionsRouterModule,
        StPaginationModule,
        SharedModule,
        ConsoleBoxModule
    ],
    providers: [reducerProvider]
})

export class ExecutionsMonitoringModule {
}
