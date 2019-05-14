/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  Component,
} from '@angular/core';
import { appInfo } from '@app/shared/constants/appInfo';
import { BreadcrumbMenuService } from 'services/*';

@Component({
  selector: 'about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AboutComponent {
  public info = appInfo;
  public breadcrumbOptions: string[] = [];

  constructor(public breadcrumbMenuService: BreadcrumbMenuService) {
    this.breadcrumbOptions = breadcrumbMenuService.getOptions();
  }
}
