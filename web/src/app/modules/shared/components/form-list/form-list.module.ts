
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { StCheckboxModule, StSwitchModule, StTextareaModule } from '@stratio/egeo';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

import { FormListComponent } from './form-list.component';
import { HighlightTextareaModule} from './../highlight-textarea/hightlight-textarea.module';
import { SpInputModule } from './../sp-input/sp-input.module';
import { SpColumnInputModule } from './../sp-column-input/sp-column-input.module';
import { SpSelectModule } from './../sp-select/sp-select.module';

import { ErrorMessagesService } from 'app/services';


@NgModule({
   exports: [
      FormListComponent,
   ],
   imports: [
      CommonModule,
      FormsModule,
      ReactiveFormsModule,
      SpInputModule,
      StTextareaModule,
      SpSelectModule,
      StCheckboxModule,
      StSwitchModule,
      HighlightTextareaModule,
      TranslateModule,
      SpColumnInputModule
   ],
   declarations: [
      FormListComponent
   ],
   providers: [
     ErrorMessagesService
   ]
})

export class FormListModule { }
