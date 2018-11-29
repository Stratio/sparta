/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { StCheckboxModule, StFullscreenLayoutModule, StSearchModule, StTagInputModule } from '@stratio/egeo';
import { TranslateModule } from '@ngx-translate/core';

import { WizardSettingsComponent } from './wizard-settings.component';
import { ParametersGroupSelectorComponent } from './parameters-group-selector/parameters-group-selector.component';
import { FormGeneratorModule } from '@app/shared/components/form-generator/form-generator.module';
import { SpHelpModule } from '@app/shared/components/sp-help/sp-help.module';

@NgModule({
  exports: [
    WizardSettingsComponent
  ],
  declarations: [
    WizardSettingsComponent,
    ParametersGroupSelectorComponent
  ],
  imports: [
    CommonModule,
    FormGeneratorModule,
    FormsModule,
    ReactiveFormsModule,
    SpHelpModule,
    StCheckboxModule,
    StFullscreenLayoutModule,
    StSearchModule,
    StTagInputModule,
    TranslateModule
  ]
})

export class WizardSettingsModule {}
