/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { StProgressBarModule, StDropdownMenuModule } from '@stratio/egeo';
import { WizardHeaderComponent } from './wizard-header.component';

import { WizardPositionToolModule } from '@app/wizard/components/wizard-position-tool/wizard-position-tool.module';
import { SpTitleModule, FloatingMenuModule, MenuOptionsListModule } from '@app/shared';


@NgModule({
  exports: [
    WizardHeaderComponent
  ],
  declarations: [
    WizardHeaderComponent
  ],
  imports: [
    CommonModule,
    FloatingMenuModule,
    MenuOptionsListModule,
    StProgressBarModule,
    SpTitleModule,
    TranslateModule,
    WizardPositionToolModule
  ]
})

export class WizardHeaderModule {}
