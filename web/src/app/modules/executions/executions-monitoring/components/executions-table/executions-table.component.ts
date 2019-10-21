/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    Output
} from '@angular/core';
import { StTableHeader, PaginateOptions, Order } from '@stratio/egeo';
import { Router } from '@angular/router';
import {Engine, Execution} from '@models/enums';

@Component({
    selector: 'executions-table',
    styleUrls: ['executions-table.component.scss'],
    templateUrl: 'executions-table.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsTableComponent {

   @Input() executionList: Array<any> = [];

   public fields: StTableHeader[];
   public Engine = Engine;
   public ExecutionType = Execution;

   public loadingStates: string[] = ['Starting', 'Launched', 'Uploaded'];

   constructor(private route: Router, private _cd: ChangeDetectorRef) {
      this.fields = [
         { id: 'name', label: 'Name' },
         { id: 'filterStatus', label: 'Context' },
         { id: 'startDateMillis', label: 'Start Date' },
         { id: 'endDateMillis', label: 'End Date' },
         { id: 'filterStatus', label: 'Status' },
         { id: 'spark', label: '', sortable: false }
      ];
   }

   navigateExecutions() {
      this.route.navigate(['executions']);
   }

  showURI (url: string) {
    window.open(url, '_blank');
  }

}
