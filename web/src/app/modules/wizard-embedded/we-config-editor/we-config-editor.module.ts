/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { StFullscreenLayoutModule } from '@stratio/egeo';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';


import { WeConfigEditorComponent } from '@app/wizard-embedded/we-config-editor/we-config-editor.component';
import { FormGeneratorModule } from '@app/shared/components/form-generator/form-generator.module';
import { SpInputModule } from '@app/shared/components/sp-input/sp-input.module';
import { SpTextareaModule } from '@app/shared/components/sp-textarea/sp-textarea.module';
import { SidebarConfigModule } from '@app/wizard/components/sidebar-config/sidebar-config.module';
import { SpHelpModule } from '@app/shared/components/sp-help/sp-help.module';

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
    SpHelpModule,
    SpInputModule,
    SpTextareaModule,
    SidebarConfigModule,
    TranslateModule
  ]
})

export class WeConfigEditorModule {}
