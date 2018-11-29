/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { CommonModule } from '@angular/common';
import {
   StBreadcrumbsModule, StTableModule, StCheckboxModule, StRadioMenuModule, StRadioModule,
   StPaginationModule, StProgressBarModule, StFullscreenLayoutModule, StTextareaModule, StInputModule, StHorizontalTabsModule
} from '@stratio/egeo';
import { FormsModule } from '@angular/forms';
import { EffectsModule } from '@ngrx/effects';
import { TranslateModule } from '@ngx-translate/core';

import { SpHelpModule, TableNotificationModule, ToolBarModule, SpSelectModule, FormGeneratorModule } from '@app/shared';

import { TemplatesComponent } from './templates.component';
import { TemplatesRoutingModule } from './templates-routing.module';

import { InputsComponent } from './components/template-list/inputs.component';
import { OutputsComponent } from './components/template-list/outputs.component';
import { TransformationsComponent } from './components/template-list/transformations.component';
import { CreateInputComponent } from './components/template-creation/create-input.component';
import { CreateOutputComponent } from './components/template-creation/create-output.component';
import { CreateTransformationsComponent } from './components/template-creation/create-transformation.component';
import { reducers } from './reducers';
import { InputEffect } from './effects/input';
import { TransformationEffect } from './effects/transformation';
import { OutputEffect } from './effects/output';

@NgModule({
   declarations: [
      TemplatesComponent,
      InputsComponent,
      OutputsComponent,
      TransformationsComponent,
      CreateInputComponent,
      CreateOutputComponent,
      CreateTransformationsComponent
   ],
   imports: [
      CommonModule,
      FormsModule,
      StoreModule.forFeature('templates', reducers),
      EffectsModule.forFeature([InputEffect, TransformationEffect, OutputEffect]),
      TemplatesRoutingModule,
      TableNotificationModule,
      FormGeneratorModule,
      SpHelpModule,
      SpSelectModule,
      ToolBarModule,
      StRadioMenuModule,
      StProgressBarModule,
      StFullscreenLayoutModule,
      StPaginationModule,
      StRadioModule,
      StTextareaModule,
      StInputModule,
      StBreadcrumbsModule,
      StHorizontalTabsModule,
      StTableModule,
      StCheckboxModule,
      TranslateModule
   ]
})
export class TemplatesModule { }
