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
import { SpSelectModule } from '@app/shared/components/sp-select/sp-select.module';
import { HighlightTextareaModule } from '@app/shared/components/highlight-textarea/hightlight-textarea.module';
import { TableNotificationModule } from '@app/shared/components/table-notification/table-notification.module';
import { FormGeneratorModule } from '@app/shared/components/form-generator/form-generator.module';
import { FormListModule } from '@app/shared/components/form-list/form-list.module';
import { FormFieldModule } from '@app/shared/components/form-field/form-field.module';
import { SpTitleModule } from '@app/shared/components/sp-title/sp-title.module';
import { SpHelpModule } from '@app/shared/components/sp-help/sp-help.module';
import { ConsoleBoxModule } from '@app/shared/components/console-box/console.box.module';
import { ToolBarModule } from '@app/shared/components/tool-bar/tool-bar.module';
import { FormFileModule } from '@app/shared/components/form-file/form-file.module';
import { SpartaSidebarModule } from '@app/shared/components/sparta-sidebar/sparta-sidebar.module';

@NgModule({
   exports: [
      CommonModule,
      TranslateModule,
      ...shareComponents,
      SpInputModule,
      SpTextareaModule,
      SpSelectModule,
      SpTitleModule,
      SpHelpModule,
      ToolBarModule,
      FormGeneratorModule,
      FormFieldModule,
      FormFileModule,
      FormListModule,
      TableNotificationModule,
      SpartaSidebarModule,
      ConsoleBoxModule
   ],
   imports: [
      CommonModule,
      FormsModule,
      FormListModule,
      FormFieldModule,
      FormGeneratorModule,
      SpHelpModule,
      ReactiveFormsModule,
      SpInputModule,
      SpTextareaModule,
      SpartaSidebarModule,
      ToolBarModule,
      FormFileModule,
      HighlightTextareaModule,
      SpSelectModule,
      TranslateModule,
      TableNotificationModule,
      EgeoModule,
      ConsoleBoxModule
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
