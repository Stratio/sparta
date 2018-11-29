/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { EgeoModule } from '@stratio/egeo';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SpInputModule } from './components/sp-input/sp-input.module';
import { SpTextareaModule } from './components/sp-textarea/sp-textarea.module';
import { SpSelectModule } from './components/sp-select/sp-select.module';
import { HighlightTextareaModule } from './components/highlight-textarea/hightlight-textarea.module';
import { TableNotificationModule } from './components/table-notification/table-notification.module';
import { FormGeneratorModule } from './components/form-generator/form-generator.module';
import { FormListModule } from './components/form-list/form-list.module';
import { FormFieldModule } from './components/form-field/form-field.module';
import { SpTitleModule } from './components/sp-title/sp-title.module';
import { SpHelpModule } from './components/sp-help/sp-help.module';
import { ConsoleBoxModule } from './components/console-box/console.box.module';
import { ToolBarModule } from './components/tool-bar/tool-bar.module';
import { FormFileModule } from './components/form-file/form-file.module';
import { SpartaSidebarModule } from './components/sparta-sidebar/sparta-sidebar.module';
import { SpColumnInputModule } from './components/sp-column-input/sp-column-input.module';
import {LoadingSpinnerModule} from './components/loading-spinner/loading-spinner.module';
import { EmptyTableBoxModule } from './components/empty-table-box/empty-table-box.module';
import { NotificationAlertModule } from './components/notification-alert/notification-alert.module';

@NgModule({
   exports: [
      CommonModule,
      TranslateModule,
      SpInputModule,
      SpTextareaModule,
      SpSelectModule,
      SpTitleModule,
      SpHelpModule,
      ToolBarModule,
      FormGeneratorModule,
      FormFieldModule,
      LoadingSpinnerModule,
      FormFileModule,
      FormListModule,
      TableNotificationModule,
      SpartaSidebarModule,
      ConsoleBoxModule,
      SpColumnInputModule,
      EmptyTableBoxModule
   ],
   imports: [
      CommonModule,
      FormsModule,
      FormListModule,
      FormFieldModule,
      FormGeneratorModule,
      SpHelpModule,
      LoadingSpinnerModule,
      ReactiveFormsModule,
      SpInputModule,
      SpTextareaModule,
      SpartaSidebarModule,
      ToolBarModule,
      FormFileModule,
      NotificationAlertModule,
      HighlightTextareaModule,
      SpSelectModule,
      TranslateModule,
      TableNotificationModule,
      EgeoModule,
      ConsoleBoxModule,
      SpColumnInputModule,
      EmptyTableBoxModule
   ],
   providers: []
})

export class SharedModule {

   constructor(translate: TranslateService) {
      // TODO: remove hardcode lang when allow multilanguage
      // let userLang = translate.getBrowserLang();
      translate.setDefaultLang('en');
      translate.use('en');
   }
}
