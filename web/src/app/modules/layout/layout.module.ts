/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StHeaderModule, StPopOverModule } from '@stratio/egeo';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

import { NotificationAlertModule } from '@app/shared/components/notification-alert/notification-alert.module';

import { LayoutComponent, LayoutRouter, UserProfileComponent } from '.';
import { MenuService } from '@app/shared/services/menu.service';

@NgModule({
   declarations: [
      LayoutComponent,
      UserProfileComponent
   ],
   imports: [
      CommonModule,
      StHeaderModule,
      StPopOverModule,
      FormsModule,
      LayoutRouter,
      NotificationAlertModule,
      TranslateModule
   ],
   providers: [
     MenuService
   ]
})

export class LayoutModule { }
