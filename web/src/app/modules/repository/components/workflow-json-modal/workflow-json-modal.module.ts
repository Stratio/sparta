/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PerfectScrollbarModule } from 'ngx-perfect-scrollbar';
import { StTextareaModule } from '@stratio/egeo';
import { TranslateModule } from '@ngx-translate/core';

import { FileReaderModule, SpInputModule } from '@app/shared';
import { WorkflowJsonModalComponent } from './workflow-json-modal.component';



@NgModule({
   imports: [
     CommonModule,
     FileReaderModule,
     FormsModule,
     PerfectScrollbarModule,
     SpInputModule,
     StTextareaModule,
     TranslateModule
   ],
   declarations: [WorkflowJsonModalComponent],
   exports: [WorkflowJsonModalComponent]
})
export class WorkflowJsonModalModule { }
