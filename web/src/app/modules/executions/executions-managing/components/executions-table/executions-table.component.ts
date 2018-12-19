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
  Output,
  ViewEncapsulation
} from '@angular/core';
import { StTableHeader, Order } from '@stratio/egeo';
import { Router } from '@angular/router';

@Component({
   selector: 'executions-managing-table',
   styleUrls: ['executions-table.component.scss'],
   templateUrl: 'executions-table.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush,
   encapsulation: ViewEncapsulation.None
})

export class ExecutionTableComponent {

  @Input() executionList: Array<any> = [];
  @Input() selectedExecutionsIds: Array<string> = [];
  @Input() perPage = 0;
  @Input() currentPage = 0;
  @Input() total = 0;
  @Input() currentOrder;
  @Input() areAllSelected: Boolean = false;

  @Output() selectExecution = new EventEmitter<any>();
  @Output() deselectExecution = new EventEmitter<any>();
  @Output() onChangeOrder = new EventEmitter<Order>();
  @Output() onChangePage = new EventEmitter<any>();
  @Output() allExecutionsToggled = new EventEmitter<any>();

  public fields: StTableHeader[];
  public generatedId: string;
  public perPageOptions: any = [
    { value: 10, showFrom: 0 }, { value: 20, showFrom: 0 }, { value: 30, showFrom: 0 }
  ];

  public loadingStates: string[] = ['Starting', 'Launched', 'Uploaded'];


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

  toggleAllExecutions(isChecked: boolean) {
    this.allExecutionsToggled.emit(isChecked);
    this.areAllSelected = isChecked;
  }

  showSparkUI(url: string) {
    window.open(url, '_blank');
  }

  trackByFn(index, item) {
    return item.id;
  }

  constructor(private route: Router, private _cd: ChangeDetectorRef) {
    this.generatedId = 'paginator-' + Math.floor((Math.random() * 1000) + 1);
    this.fields = [
      { id: 'name', label: 'Name' },
      { id: 'context', label: 'Context' },
      { id: 'launchDateMillis', label: 'Launch Date' },
      { id: 'endDateMillis', label: 'End Date' },
      { id: 'filterStatus', label: 'Status' },
      { id: 'spark', label: '', sortable: false }
    ];
  }

  goToWorkflow(ev, id) {
    ev.preventDefault();
    ev.stopPropagation();
    this.route.navigate(['executions', id]);
  }
}
