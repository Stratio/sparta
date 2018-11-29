/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { StFullscreenLayoutModule, StHorizontalTabsModule } from '@stratio/egeo';

import { WizardConfigEditorComponent } from './wizard-config-editor.component';
import { MocksConfigModule } from './mocks-config/mocks-config.module';

import { FormGeneratorModule } from '@app/shared/components/form-generator/form-generator.module';
import { SpHelpModule } from '@app/shared/components/sp-help/sp-help.module';
import { SpInputModule } from '@app/shared/components/sp-input/sp-input.module';
import { SpTextareaModule } from '@app/shared/components/sp-textarea/sp-textarea.module';
import { QueryBuilderModule } from '@app/wizard/components/query-builder/query-builder.module';
import { SidebarConfigModule } from '@app/wizard/components/sidebar-config/sidebar-config.module';

@NgModule({
  exports: [
    WizardConfigEditorComponent
  ],
  declarations: [
    WizardConfigEditorComponent
  ],
  imports: [
    CommonModule,
    FormGeneratorModule,
    FormsModule,
    QueryBuilderModule,
    MocksConfigModule,
    SidebarConfigModule,
    SpHelpModule,
    SpInputModule,
    SpTextareaModule,
    StFullscreenLayoutModule,
    StHorizontalTabsModule,
    TranslateModule
  ]
})

export class WizardConfigEditorModule {}
