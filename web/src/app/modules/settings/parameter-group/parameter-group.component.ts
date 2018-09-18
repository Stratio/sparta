/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
   ChangeDetectionStrategy,
   Component,
} from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo/';
import { BreadcrumbMenuService } from 'app/services';

@Component({
   selector: 'parameter-group',
   templateUrl: './parameter-group.component.html',
   styleUrls: ['./parameter-group.component.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class ParameterGroupComponent {

   public selectedOption = 'global';
   public breadcrumbOptions;
   public tabsOptions: StHorizontalTab[] = [
      {
         id: 'global',
         text: 'Global'
      }, {
         id: 'environment',
         text: 'Environment',
      },
      {
         id: 'custom',
         text: 'Custom'
      }
   ];

   constructor(public breadcrumbMenuService: BreadcrumbMenuService) {
      this.breadcrumbOptions = breadcrumbMenuService.getOptions();
   }

   changeTabOption(event: StHorizontalTab) {
      this.selectedOption = event.id;
   }

}
