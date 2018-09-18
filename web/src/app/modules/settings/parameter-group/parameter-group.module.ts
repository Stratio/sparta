/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { StHorizontalTabsModule, StBreadcrumbsModule, StSearchModule, StInputModule, StCheckboxModule, StDropdownMenuModule, StFullscreenLayoutModule } from '@stratio/egeo';

import { ParameterGroupComponent } from './parameter-group.component';
import { ParameterGroupRoutingModule } from './parameter-group.router';
import { ToolBarModule } from '@app/shared/components/tool-bar/tool-bar.module';
import { FormFileModule } from '@app/shared/components/form-file/form-file.module';
import { reducers } from './reducers/';

import { GlobalParametersContainer } from './components/global-parameters/global-parameters.container';
import { GlobalParametersComponent } from './components/global-parameters/global-parameters.component';
import { EnvironmentParametersContainer } from './components/environment-parameters/environment-parameters.container';
import { EnvironmentParametersComponent } from './components/environment-parameters/environment-parameters.component';
import { CustomParametersContainer } from './components/custom-parameters/custom-parameters.container';
import { CustomParametersComponent } from './components/custom-parameters/custom-parameters.component';
import { ParameterGroupHeaderComponent } from './components/parameter-group-header.ts/parameter-group-header.component';
import { ParametersTableComponent } from './components/parameters-table/parameters-table.component';


import { GlobalParametersEffect } from './effects/global';
import { EnviromentParametersEffect } from './effects/environment';
import { CustomParametersEffect } from './effects/custom';

@NgModule({
   declarations: [
      ParameterGroupComponent,
      CustomParametersContainer,
      CustomParametersComponent,
      GlobalParametersContainer,
      GlobalParametersComponent,
      EnvironmentParametersContainer,
      EnvironmentParametersComponent,
      ParameterGroupHeaderComponent,
      ParametersTableComponent
   ],
   imports: [
      CommonModule,
      ParameterGroupRoutingModule,
      FormFileModule,
      StBreadcrumbsModule,
      StHorizontalTabsModule,
      StSearchModule,
      StoreModule.forFeature('parameterGroup', reducers),
      EffectsModule.forFeature([GlobalParametersEffect, EnviromentParametersEffect, CustomParametersEffect]),
      ToolBarModule,
      TranslateModule,
      StInputModule,
      FormsModule,
      ReactiveFormsModule,
      StCheckboxModule,
      StDropdownMenuModule,
      StFullscreenLayoutModule
   ]
})

export class ParameterGroupModule {
   constructor() {

   }
}

