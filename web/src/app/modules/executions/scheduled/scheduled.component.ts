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
import { workflowTypesFilterOptions, timeIntervalsFilterOptions } from './models/schedules-filters';
import { ScheduledExecution } from './models/scheduled-executions';

@Component({
  selector: 'scheduled-list',
  styleUrls: ['scheduled.component.scss'],
  templateUrl: 'scheduled.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})

export class ScheduledComponent {

  @Input() typeFilter: StDropDownMenuItem;
  @Input() timeIntervalFilter: StDropDownMenuItem;
  @Input() searchQuery: string;
  @Input() areAllSelected: boolean;
  @Input() currentOrder: any;
  @Input() scheduledExecutions: Array<ScheduledExecution>;
  @Input() selectedExecutions: Array<string>;

  @Output() onChangeTypeFilter = new EventEmitter<any>();
  @Output() onChangeTimeIntervalFilter = new EventEmitter<number>();
  @Output() onSearch = new EventEmitter<any>();
  @Output() selectExecution = new EventEmitter<string>();
  @Output() allExecutionsToggled = new EventEmitter<boolean>();
  @Output() onChangeOrder = new EventEmitter<Order>();

  public fields: StTableHeader[];
  public showedFilter = '';

  public workflowTypes: StDropDownMenuItem[] = workflowTypesFilterOptions;
  public timeIntervals: StDropDownMenuItem[] = timeIntervalsFilterOptions;

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
    this.allExecutionsToggled.emit(isChecked);
    this.areAllSelected = isChecked;
  }

  @HostListener('document:click', ['$event'])
  onClick() {
    this.showedFilter = '';
  }


}
