/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {NgModule} from '@angular/core';
import {WizardNotificationsComponent} from '@app/wizard/components/wizard-notifications/wizard-notifications.component';
import {SpForegroundNotificationsModule} from '@app/shared/components/sp-foreground-notifications/sp-foreground-notifications.module';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import {LoadingSpinnerModule} from '@app/shared/components/loading-spinner/loading-spinner.module';

@NgModule({
  exports: [
    WizardNotificationsComponent
  ],
  declarations: [
    WizardNotificationsComponent
  ],
  imports: [
    SpForegroundNotificationsModule,
    CommonModule,
    TranslateModule,
    LoadingSpinnerModule
  ]
})

export class WizardNotificationsModule {}
