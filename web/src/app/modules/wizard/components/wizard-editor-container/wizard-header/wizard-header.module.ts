/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { StProgressBarModule } from '@stratio/egeo';
import { WizardHeaderComponent } from './wizard-header.component';

import { FloatingMenuModule } from '@app/shared/components/floating-menu/floating-menu.module';
import { MenuOptionsListModule } from '@app/shared/components/menu-options-list/menu-options-list.module';


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
    TranslateModule
  ]
})

export class WizardHeaderModule {}
