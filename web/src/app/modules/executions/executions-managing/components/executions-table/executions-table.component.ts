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

@Component({
    selector: 'executions-managing-table',
    styleUrls: ['executions-table.component.scss'],
    templateUrl: 'executions-table.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsTableComponent {

   @Input() executionList: Array<any> = [];
   @Input() selectedExecutionsIds: Array<string> = [];

   @Output() selectExecution = new EventEmitter<any>();
   @Output() deselectExecution = new EventEmitter<any>();

   public fields: StTableHeader[];
   public generatedId: string;

   checkValue(event: any) {
      this.checkRow(event.checked, event.value);
   }

   checkRow(isChecked: boolean, value: any) {
      if (isChecked) {
         this.selectExecution.emit(value);
      } else {
         this.deselectExecution.emit(value);
      }
   }

   onChangeOrder(ev) { }

   showSparkUI(url: string) {
      window.open(url, '_blank');
   }

      constructor(private route: Router, private _cd: ChangeDetectorRef) {
      this.generatedId = 'paginator-' + Math.floor((Math.random() * 1000) + 1);
      this.fields = [
         { id: 'isChecked', label: '', sortable: false },
         { id: 'name', label: 'Name' },
         { id: 'filterStatus', label: 'Context' },
         { id: 'startDateMillis', label: 'Start Date' },
         { id: 'endDateMillis', label: 'End Date' },
         { id: 'filterStatus', label: 'Status' },
         { id: 'spark', label: '', sortable: false }
      ];
   }
}
