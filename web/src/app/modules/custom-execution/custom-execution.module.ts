/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StSearchModule, StFullscreenLayoutModule, StInputModule } from '@stratio/egeo';
import { TranslateModule } from '@ngx-translate/core';

import { SpSelectModule } from '../shared/components/sp-select/sp-select.module';
import { CustomExecutionContainer } from './custom-execution.container';
import { CustomExecutionComponent } from './custom-execution.component';

import { SpartaSidebarModule } from '@app/shared/components/sparta-sidebar/sparta-sidebar.module';

@NgModule({
  exports: [
    CustomExecutionContainer
  ],
  declarations: [
    CustomExecutionContainer,
    CustomExecutionComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    StSearchModule,
    StInputModule,
    StFullscreenLayoutModule,
    SpSelectModule,
    SpartaSidebarModule,
    TranslateModule
  ]
})

export class CustomExecutionModule { }
