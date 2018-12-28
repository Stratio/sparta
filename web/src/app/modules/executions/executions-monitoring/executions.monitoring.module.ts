/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { EffectsModule } from '@ngrx/effects';

import { reducerToken, reducerProvider } from './reducers';
import { ExecutionsEffect } from './effects/executions';

import { ExecutionHelperService } from 'app/services/helpers/execution.service';
import { EmptyTableBoxModule } from '@app/shared';

import { ExecutionsMonitoringHeaderModule } from './components/executions-header/executions-header.module';
import { ExecutionsMonitoringTableModule } from './components/executions-table/executions-table.module';
import { ExecutionsChartModule } from './components/executions-chart/executions-chart.module';

import { ExecutionsComponent } from './executions.component';
import { ExecutionsRouterModule } from './executions.router';
import { ExecutionPeriodsService } from './services/execution-periods.service';


@NgModule({
    declarations: [
      ExecutionsComponent
    ],
    imports: [
        CommonModule,
        ExecutionsMonitoringHeaderModule,
        ExecutionsMonitoringTableModule,
        ExecutionsChartModule,
        EmptyTableBoxModule,
        StoreModule.forFeature('executionsMonitoring', reducerToken),
        EffectsModule.forFeature([ExecutionsEffect]),
        ExecutionsRouterModule,
        TranslateModule
    ],
    providers: [ExecutionHelperService, ExecutionPeriodsService, reducerProvider]
})

export class ExecutionsMonitoringModule {
}
