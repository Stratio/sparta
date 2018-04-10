
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { StHorizontalTabsModule } from '@stratio/egeo';

import { FormGeneratorComponent } from './form-generator.component';
import { FormGeneratorGroupComponent } from './form-generator-group/form-generator-group.component';
import { FormFieldModule } from '@app/shared/components/form-field/form-field.module';

@NgModule({
   exports: [
      FormGeneratorComponent,
      FormGeneratorGroupComponent
   ],
   imports: [
      CommonModule,
      FormsModule,
      ReactiveFormsModule,
      FormFieldModule,
      TranslateModule,
      StHorizontalTabsModule
   ],
   declarations: [
      FormGeneratorComponent,
      FormGeneratorGroupComponent
   ]
})

export class FormGeneratorModule { }
