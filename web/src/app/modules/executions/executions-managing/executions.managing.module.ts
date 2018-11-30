/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { CommonModule } from '@angular/common';
import { EffectsModule } from '@ngrx/effects';
import { TranslateModule } from '@ngx-translate/core';


import { reducerToken, reducerProvider } from './reducers';
import { ExecutionsEffect } from './effects/executions';
import { ExecutionHelperService } from 'app/services/helpers/execution.service';
import { ExecutionsDetailComponent } from './components/executions-detail/executions-detail.component';

import { EmptyTableBoxModule, SpartaSidebarModule, GraphEditorModule } from '@app/shared';

import { ExecutionsConsoleModule } from './components/executions-console/executions-console.module';
import { ExecutionsSidebarDetailModule } from './components/executions-sidebar-detail/executions-sidebar-detail.module';
import { ExecutionInfoModule } from './components/execution-info/execution-info.module';
import { ExecutionsHeaderModule } from './components/executions-header/executions-header.module';
import { ExecutionsManagingTableModule } from './components/executions-table/executions-table.module';

import { ExecutionsManagingComponent } from './executions.component';


import { ExecutionsRouterModule } from './executions.router';
import { EdgeOptionsModule } from '@app/wizard/components/edge-options/edge-options.module';
import { WizardNodeModule } from '@app/wizard/components/wizard-node/wizard-node.module';
import { WizardEdgeModule } from '@app/wizard/components/wizard-edge/wizard-edge.module';


@NgModule({
   declarations: [
      ExecutionsManagingComponent,
      ExecutionsDetailComponent
   ],
   imports: [
      CommonModule,
      EmptyTableBoxModule,
      SpartaSidebarModule,
      ExecutionsConsoleModule,
      ExecutionsSidebarDetailModule,
      ExecutionInfoModule,
      ExecutionsManagingTableModule,
      ExecutionsHeaderModule,
      StoreModule.forFeature('executions', reducerToken),
      EffectsModule.forFeature([ExecutionsEffect]),
      ExecutionsRouterModule,
      TranslateModule,
      GraphEditorModule,
      WizardNodeModule,
      EdgeOptionsModule,
      WizardEdgeModule
   ],
   providers: [reducerProvider, ExecutionHelperService]
})

export class ExecutionsManagingModule {
}
