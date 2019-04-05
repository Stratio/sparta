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
  ViewEncapsulation,
  HostListener
} from '@angular/core';
import { StTableHeader, Order, StDropDownMenuItem } from '@stratio/egeo';
import { workflowTypesFilterOptions, timeIntervalsFilterOptions, activeFilterOptions } from './models/schedules-filters';
import { ScheduledExecution } from './models/scheduled-executions';
import { MenuOptionListGroup } from '@app/shared/components/menu-options-list/menu-options-list.component';

@Component({
  selector: 'scheduled-list',
  styleUrls: ['scheduled.component.scss'],
  templateUrl: 'scheduled.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})

export class ScheduledComponent {

  @Input() typeFilter: StDropDownMenuItem;
  @Input() activeFilter: StDropDownMenuItem;
  @Input() searchQuery: string;
  @Input() currentOrder: Order;
  @Input() scheduledExecutions: Array<ScheduledExecution>;
  @Input() selectedExecutions: Array<string>;
  @Input() isEmptyScheduledExecutions: boolean;

  @Output() onChangeTypeFilter = new EventEmitter<any>();
  @Output() onChangeActiveFilter = new EventEmitter<number>();
  @Output() onSearch = new EventEmitter<any>();
  @Output() selectExecution = new EventEmitter<string>();
  @Output() allExecutionsToggled = new EventEmitter<Array<string>>();
  @Output() onChangeOrder = new EventEmitter<Order>();
  @Output() stopExecution = new EventEmitter<ScheduledExecution>();
  @Output() startExecution = new EventEmitter<ScheduledExecution>();
  @Output() deleteExecution = new EventEmitter<string>();

  public fields: StTableHeader[];
  public showedFilter = '';

  public workflowTypes: StDropDownMenuItem[] = workflowTypesFilterOptions;
  public activeOptions: StDropDownMenuItem[] = activeFilterOptions;

  public executionOptions: MenuOptionListGroup[] = [
    {
      options: [
        {
          icon: 'icon-trash',
          label: 'Delete',
          id: 'scheduled-delete',
          color: 'critical'
        }
      ]
    }
  ];
  public activeExecutionOptions = [{
    options: [
      {
        icon: 'icon-stop',
        label: 'Stop',
        id: 'scheduled-stop'
      }
    ]
  },
  ...this.executionOptions
  ]
  public inactiveExecutionOptions = [{
    options: [
      {
        icon: 'icon-play',
        label: 'Play',
        id: 'scheduled-start'
      }
    ]
  },
  ...this.executionOptions
  ]

  constructor(private _cd: ChangeDetectorRef) {
    this.fields = [
      { id: 'name', label: 'Workflow' },
      { id: 'initDateMillis', label: 'Init date' },
      { id: 'duration', label: 'Repeat' },
      { id: 'active', label: 'Active' },
      { id: 'executed', label: 'Status' },
      { id: 'spark', label: '', sortable: false }
    ];
  }

  selectFilter(event: any, filter: string) {
    event.stopPropagation();
    this.showedFilter = filter;
  }

  toggleAllExecutions(isChecked: boolean) {
    if(isChecked) {
      this.allExecutionsToggled.emit(this.scheduledExecutions.map(execution => execution.id));
    } else {
      this.allExecutionsToggled.emit([]);
    }
  }

  selectedExecutionAction(event, execution: ScheduledExecution) {
    switch(event) {
      case 'scheduled-stop':
        this.stopExecution.emit(execution);
        break;
      case 'scheduled-start':
        this.startExecution.emit(execution);
        break;
      case 'scheduled-delete':
        this.deleteExecution.emit(execution.id);
        break;
    }
  }

  deleteMultiple() {
    this.selectedExecutions.forEach(executionId => {
      this.deleteExecution.emit(executionId);
    });
  }

  @HostListener('document:click', ['$event'])
  onClick() {
    this.showedFilter = '';
  }


}
