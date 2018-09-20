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
import { Router } from '@angular/router';
import { StModalService, StDropDownMenuItem } from '@stratio/egeo';

import { BreadcrumbMenuService } from 'services';

@Component({
   selector: 'executions-managing-header',
   styleUrls: ['executions-header.component.scss'],
   templateUrl: 'executions-header.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush,
   host: {
      '(document:click)': 'onClick($event)'
   }
})

export class ExecutionsHeaderComponent {

   @Input() selectedExecutions: Array<any> = [];
   @Input() showDetails = false;


   @Input() get statusFilter() {
      return this._statusFilter;
   }
   set statusFilter(value) {
      this.statusFilterItem = this.getDropdownItem(this.statuses, value);
      this._statusFilter = value;
   }


   @Input() get typeFilter() {
      return this._typeFilter;
   }
   set typeFilter(value) {
      this.typeFilterItem = this.getDropdownItem(this.workflowTypes, value);
      this._typeFilter = value;
   }


   @Input() get timeIntervalFilter () {
      return this._timeIntervalFilter;
   }
   set timeIntervalFilter(value) {
      this.timeIntervalFilterItem = this.getDropdownItem(this.timeIntervals, value);
      this._timeIntervalFilter = value;
   }


   @Output() downloadExecutions = new EventEmitter<void>();
   @Output() onRunExecutions = new EventEmitter<any>();
   @Output() onStopExecution = new EventEmitter<any>();
   @Output() onSearch = new EventEmitter<any>();
   @Output() showExecutionInfo = new EventEmitter<void>();

   @Output() onChangeStatusFilter = new EventEmitter<string>();
   @Output() onChangeTypeFilter = new EventEmitter<string>();
   @Output() onChangeTimeIntervalFilter = new EventEmitter<number>();

   public searchQuery = '';

   public statusFilterItem: StDropDownMenuItem;
   public typeFilterItem: StDropDownMenuItem;
   public timeIntervalFilterItem: StDropDownMenuItem;

   public showedFilter = '';
   public statuses: StDropDownMenuItem[] = [
      {
         label: 'all status',
         value: ''
      },
      {
         label: 'running',
         value: 'Running'
      },
      {
         label: 'stopped',
         value: 'Stopped'
      },
      {
         label: 'failed',
         value: 'Failed'
      },
      {
         label: 'archived',
         value: 'Archived'
      }
   ];
   public workflowTypes: StDropDownMenuItem[] = [
      {
         label: 'all types',
         value: ''
      },
      {
         label: 'streaming',
         value: 'Streaming'
      },
      {
         label: 'batch',
         value: 'Batch'
      }
   ];
   public timeIntervals: StDropDownMenuItem[] = [
      {
         label: 'any time',
         value: 0
      },
      {
         label: 'last 60 minutes',
         value: 3600000
      },
      {
         label: 'last 6 hours',
         value: 21600000
      },
      {
         label: 'last 24 hours',
         value: 86400000
      },
      {
         label: 'last 3 days',
         value: 259200000
      },
      {
         label: 'last 7 days',
         value: 604800000
      }
   ];
   public breadcrumbOptions: string[] = [];

   private _statusFilter = '';
   private _typeFilter = '';
   private _timeIntervalFilter = 0;

   constructor(private _modalService: StModalService, public breadcrumbMenuService: BreadcrumbMenuService, private route: Router) {
      this.breadcrumbOptions = ['Home', 'executions'];
   }

   selectFilter(event: any, filter: string) {
      event.stopPropagation();
      this.showedFilter = filter;
   }

   public getDropdownItem(list: StDropDownMenuItem[], value) {
      return list.find(item => item.value === value);
   }

   onClick() {
      this.showedFilter = '';
   }

}
