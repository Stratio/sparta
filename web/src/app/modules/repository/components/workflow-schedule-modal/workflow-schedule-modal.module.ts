/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { SpInputModule, SpSelectModule } from '@app/shared';
import { NgbDatepickerModule } from '@ng-bootstrap/ng-bootstrap';

import {WorkflowScheduleModalComponent} from "@app/repository/components/workflow-schedule-modal/workflow-schedule-modal.component";
import { StCheckboxModule } from '@stratio/egeo';

@NgModule({
   imports: [
     CommonModule,
     ReactiveFormsModule,
     StCheckboxModule,
     SpInputModule,
     SpSelectModule,
     NgbDatepickerModule
   ],
   declarations: [WorkflowScheduleModalComponent],
   exports: [WorkflowScheduleModalComponent]
})
export class WorkflowScheduleModalModule { }
