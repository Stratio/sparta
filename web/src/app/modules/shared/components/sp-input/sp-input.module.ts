/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {StModalModule, StModalService, StLabelModule, StSelectModule} from '@stratio/egeo';

import { SpInputComponent } from './sp-input.component';

import { VariableSelectorComponent } from './variable-selector/variable-selector.component';
import { SpSelectModule } from '@app/shared/components/sp-select/sp-select.module';

@NgModule({
   imports: [
     CommonModule,
     FormsModule,
     ReactiveFormsModule,
     StLabelModule,
     SpSelectModule,
     StSelectModule,
     StModalModule.withComponents([VariableSelectorComponent])
   ],
   declarations: [SpInputComponent,  VariableSelectorComponent],
   exports: [SpInputComponent],
   providers: [
     StModalService,
   ]
})
export class SpInputModule { }
