/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
    ChangeDetectionStrategy,
    Component,
    Output,
    EventEmitter,
    Input,
  } from '@angular/core';
  
  import { BreadcrumbMenuService } from 'services';

  @Component({
    selector: 'executions-toolbar',
    styleUrls: ['executions-toolbar.component.scss'],
    templateUrl: 'executions-toolbar.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
  })
  
  export class ExecutionsToolbarComponent {

    @Input() showSidebar: boolean;
    @Output() onShowSidebar = new EventEmitter();
  
    public breadcrumbOptions: string[] = [];
  
    constructor(
      public breadcrumbMenuService: BreadcrumbMenuService,
    ) {
      this.breadcrumbOptions = breadcrumbMenuService.getOptions();
    }

  
  }
  