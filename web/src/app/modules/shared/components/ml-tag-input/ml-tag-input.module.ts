/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MlTagInputComponent } from '@app/shared/components/ml-tag-input/ml-tag-input.component';

import { StLabelModule, StDropdownMenuModule } from '@stratio/egeo';
import { MlClickOutsideModule } from '@app/shared/components/ml-click-outside/ml-click-outside.module';

@NgModule({
   imports: [
     CommonModule,
     FormsModule,
     ReactiveFormsModule,
     StLabelModule,
     StDropdownMenuModule,
     MlClickOutsideModule
   ],
   declarations: [
     MlTagInputComponent
   ],
   exports: [
     MlTagInputComponent
   ]
})
export class MlTagInputModule {}
