/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

import { MessageNotificationComponent } from './message-notification.component';

@NgModule({
   imports: [
     CommonModule,
     TranslateModule
   ],
   declarations: [MessageNotificationComponent],
   exports: [MessageNotificationComponent]
})
export class MessageNotificationModule { }
