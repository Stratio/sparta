/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { WeConfigEditorComponent } from '@app/wizard-embedded/we-config-editor/we-config-editor.component';
import { StFullscreenLayoutModule } from '@stratio/egeo';
import { CommonModule } from '@angular/common';
import { FormGeneratorModule } from '@app/shared/components/form-generator/form-generator.module';
import { FormsModule } from '@angular/forms';
import { SpInputModule } from '@app/shared/components/sp-input/sp-input.module';
import { SpTextareaModule } from '@app/shared/components/sp-textarea/sp-textarea.module';
import {SharedModule} from '@app/shared';
import {SidebarConfigModule} from '@app/wizard/components/sidebar-config/sidebar-config.module';

@NgModule({
  exports: [
    WeConfigEditorComponent
  ],
  declarations: [
    WeConfigEditorComponent
  ],
  imports: [
    CommonModule,
    FormGeneratorModule,
    FormsModule,
    StFullscreenLayoutModule,
    SpInputModule,
    SpTextareaModule,
    SharedModule,
    SidebarConfigModule
  ]
})

export class WeConfigEditorModule {}
