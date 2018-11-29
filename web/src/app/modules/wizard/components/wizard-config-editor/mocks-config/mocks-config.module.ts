/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { MocksConfigComponent } from './mocks-config.component';

import { SpInputModule } from '@app/shared/components/sp-input/sp-input.module';
import { SpTextareaModule } from '@app/shared/components/sp-textarea/sp-textarea.module';
import { SidebarConfigModule } from '@app/wizard/components/sidebar-config/sidebar-config.module';
import { FileReaderModule } from '@app/shared/components/file-reader/file-reader.module';
import { HighlightTextareaModule } from '@app/shared/components/highlight-textarea/hightlight-textarea.module';
import { FormFileModule } from '@app/shared/components/form-file/form-file.module';

@NgModule({
  exports: [
    MocksConfigComponent
  ],
  declarations: [
    MocksConfigComponent
  ],
  imports: [
    CommonModule,
    FileReaderModule,
    FormFileModule,
    FormsModule,
    HighlightTextareaModule,
    SidebarConfigModule,
    SpInputModule,
    SpTextareaModule,
    TranslateModule
  ]
})

export class MocksConfigModule {}
