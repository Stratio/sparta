
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { StCheckboxModule, StSwitchModule, StTextareaModule, StInputModule } from '@stratio/egeo';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

import { SpSelectModule } from '../sp-select/sp-select.module';
import { FormListComponent } from './form-list.component';
import { HighlightTextareaModule } from '@app/shared/components/highlight-textarea/hightlight-textarea.module';


@NgModule({
   exports: [
      FormListComponent,
   ],
   imports: [
      CommonModule,
      FormsModule,
      ReactiveFormsModule,
      StInputModule,
      StTextareaModule,
      SpSelectModule,
      StCheckboxModule,
      StSwitchModule,
      HighlightTextareaModule,
      TranslateModule
   ],
   declarations: [
      FormListComponent
   ]
})

export class FormListModule { }
