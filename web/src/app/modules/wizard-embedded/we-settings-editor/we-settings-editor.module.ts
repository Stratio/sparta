/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { WeSettingsEditorComponent } from '@app/wizard-embedded/we-settings-editor/we-settings-editor.component';
import { StFullscreenLayoutModule } from '@stratio/egeo';
import { FormsModule } from '@angular/forms';
import { FormGeneratorModule } from '@app/shared/components/form-generator/form-generator.module';
import {SidebarConfigModule} from '@app/wizard/components/sidebar-config/sidebar-config.module';
import {CommonModule} from '@angular/common';
import {SpInputModule} from '@app/shared/components/sp-input/sp-input.module';
import {SpTextareaModule} from '@app/shared/components/sp-textarea/sp-textarea.module';
import {TranslateModule} from '@ngx-translate/core';
import {SpHelpModule} from '@app/shared/components/sp-help/sp-help.module';

@NgModule({
  exports: [
    WeSettingsEditorComponent
  ],
  declarations: [
    WeSettingsEditorComponent
  ],
  imports: [
    CommonModule,
    StFullscreenLayoutModule,
    FormsModule,
    FormGeneratorModule,
    SidebarConfigModule,
    SpInputModule,
    SpTextareaModule,
    TranslateModule,
    SpHelpModule
  ]
})

export class WeSettingsEditorModule {}
