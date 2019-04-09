/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
    ChangeDetectionStrategy,
    Component,
    ViewEncapsulation,
    Input,
    OnChanges,
    ChangeDetectorRef,
  } from '@angular/core';
import { ScheduledExecution } from '../../models/scheduled-executions';
import { StHorizontalTab } from '@stratio/egeo';

  
  @Component({
    selector: 'scheduled-sidebar',
    styleUrls: ['scheduled-sidebar.component.scss'],
    templateUrl: 'scheduled-sidebar.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None
  })
  
  export class ScheduledSidebarComponent  {

    @Input() showSidebar: boolean;
    @Input() executionData: ScheduledExecution;
    public options: StHorizontalTab[] = [
      {
        id: 'overview',
        text: 'Overview'
      }, {
        id: 'parameters',
        text: 'Parameters'
      }
    ];
    public activeOption = this.options[0];
    public parametersList: any = [];
    public openParameters: any = { Global: true };

    constructor(private _cd: ChangeDetectorRef) { }

    changedOption(event: StHorizontalTab) {
      this.activeOption = event;
    }
  }
  