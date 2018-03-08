/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { EgeoModule } from '@stratio/egeo';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { shareComponents, sharedProvider } from './share.declarations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SpInputModule } from '@app/shared/components/sp-input/sp-input.module';
import { SpTextareaModule } from '@app/shared/components/sp-textarea/sp-textarea.module';
import { SpTextareaComponent } from '@app/shared/components/sp-textarea/sp-textarea.component';
import { SpSelectModule } from '@app/shared/components/sp-select/sp-select.module';
import { SpInputComponent } from './components/sp-input/sp-input.component';
import { HighlightTextareaModule } from '@app/shared/components/highlight-textarea/hightlight-textarea.module';


@NgModule({
   exports: [
      CommonModule,
      TranslateModule,
      ...shareComponents,
       SpInputComponent,
       SpTextareaComponent,
       SpSelectModule
   ],
   imports: [
      CommonModule,
      FormsModule,
      ReactiveFormsModule,
      SpInputModule,
      SpTextareaModule,
      HighlightTextareaModule,
      SpSelectModule,
      TranslateModule,
      EgeoModule
   ],
   declarations: [
    ...shareComponents
   ],
   providers: [...sharedProvider]
})

export class SharedModule { 

   constructor(translate: TranslateService) {
      // TODO: remove hardcode lang when allow multilanguage
      // let userLang = translate.getBrowserLang();
      translate.setDefaultLang('en');
      translate.use('en');
   }
}
