/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
   ChangeDetectionStrategy,
   Component,
   EventEmitter,
   Input,
   Output
} from '@angular/core';


import { BreadcrumbMenuService } from 'app/services';

@Component({
   selector: 'repository-header',
   styleUrls: ['repository-header.component.scss'],
   templateUrl: 'repository-header.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class RepositoryHeaderComponent {

   @Input() showDetails = false;
   @Input() notificationMessage: any;

   @Output() showWorkflowInfo = new EventEmitter();
   @Output() hideNotification = new EventEmitter();

   public visibleNotification = true;
   public levelOptions: Array<string> = [];


   private _notificationHandler;

   constructor(public breadcrumbMenuService: BreadcrumbMenuService) {
        this.levelOptions = breadcrumbMenuService.getOptions();
   }

   public changeNotificationVisibility(visible: boolean) {
      if (!visible) {
         this.hideNotification.emit();
      } else {
         clearInterval(this._notificationHandler);
         this._notificationHandler = setTimeout(() => this.hideNotification.emit(), 5000);
      }
   }
}
