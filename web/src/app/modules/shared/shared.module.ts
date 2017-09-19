///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { EgeoModule } from '@stratio/egeo';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { shareComponents, sharedProvider } from './share.declarations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
   exports: [
      CommonModule,
      TranslateModule,
      ...shareComponents
   ],
   imports: [
      CommonModule,
      FormsModule,
      ReactiveFormsModule,
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
