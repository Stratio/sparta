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
  Output,
  OnInit,
  HostListener
} from '@angular/core';
import { Router } from '@angular/router';
import { StModalService, StDropDownMenuItem, StModalButton, StModalResponse } from '@stratio/egeo';

import { BreadcrumbMenuService } from 'services';
import { TranslateService } from '@ngx-translate/core';
import { take } from 'rxjs/operators';
import { Subscription } from 'rxjs';

@Component({
  selector: 'executions-managing-header',
  styleUrls: ['executions-header.component.scss'],
  templateUrl: 'executions-header.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsHeaderComponent implements OnInit {

  @Input() selectedExecutions: Array<any> = [];
  @Input() showDetails = false;
  @Input() showStopButton: boolean;
  @Input() showArchiveButton: boolean;
  @Input() showUnarchiveButton: boolean;
  @Input() isArchivedPage: boolean;
  @Input() emptyTable = false;
  @Input() fixSubHeaders: boolean;

  @Input() get statusFilter() {
    return this._statusFilter;
  }
  set statusFilter(value) {
    this.statusFilterItem = this.getDropdownItem(this.statuses, value);
    this._statusFilter = value;
  }


  @Input() get execTypeFilter() {
    return this._execTypeFilter;
  }
  set execTypeFilter(value) {
    this.execTypeFilterItem = this.getDropdownItem(this.executionTypes, value);
    this._execTypeFilter = value;
  }

  @Input() get wfTypeFilter() {
    return this._wfTypeFilter;
  }
  set wfTypeFilter(value) {
    this.wfTypeFilterItem = this.getDropdownItem(this.workflowTypes, value);
    this._wfTypeFilter = value;
  }


  @Input() get timeIntervalFilter() {
    return this._timeIntervalFilter;
  }
  set timeIntervalFilter(value) {
    this.timeIntervalFilterItem = this.getDropdownItem(this.timeIntervals, value);
    this._timeIntervalFilter = value;
  }


  @Output() downloadExecutions = new EventEmitter<void>();
  @Output() onReRunExecution = new EventEmitter<string>();
  @Output() onStopExecution = new EventEmitter<any>();
  @Output() onSearch = new EventEmitter<any>();
  @Output() showExecutionInfo = new EventEmitter<void>();
  @Output() archiveExecutions = new EventEmitter<void>();
  @Output() unarchiveExecutions = new EventEmitter<void>();
  @Output() onDeleteExecutions = new EventEmitter<any>();

  @Output() onChangeStatusFilter = new EventEmitter<string>();
  @Output() onChangeExecTypeFilter = new EventEmitter<string>();
  @Output() onChangeWfTypeFilter = new EventEmitter<string>();
  @Output() onChangeTimeIntervalFilter = new EventEmitter<number>();

  public searchQuery = '';

  public statusFilterItem: StDropDownMenuItem;
  public execTypeFilterItem: StDropDownMenuItem;
  public wfTypeFilterItem: StDropDownMenuItem;
  public timeIntervalFilterItem: StDropDownMenuItem;

  public showedFilter = '';
  public statuses: StDropDownMenuItem[] = [
    {
      label: 'All status',
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
    }
  ];
  public executionTypes: StDropDownMenuItem[] = [
    {
      label: 'All types',
      value: ''
    },
    {
      label: 'user',
      value: 'UserExecution'
    },
    {
      label: 'system',
      value: 'SystemExecution'
    }
  ];
  public workflowTypes: StDropDownMenuItem[] = [
    {
      label: 'All types',
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
      label: 'Launch date',
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
  private _execTypeFilter = 'UserExecution';
  private _wfTypeFilter = '';
  private _timeIntervalFilter = 0;

  private _modalSubscription: Subscription;
  private runExecutionModalHeader: string;
  private runExecutionModalOkButton: string;
  private runExecutionModalTitle: string;
  private runExecutionModalMessage: string;

  constructor(
    private _modalService: StModalService,
    public breadcrumbMenuService: BreadcrumbMenuService,
    private route: Router,
    private _translate: TranslateService
  ) {
    this.breadcrumbOptions = breadcrumbMenuService.getOptions();

    const runExecutionModalHeader = 'EXECUTIONS.RUN_EXECUTION_MODAL_HEADER';
    const runExecutionModalOkButton = 'EXECUTIONS.RUN_EXECUTION_MODAL_OK';
    const runExecutionModalTitle = 'EXECUTIONS.RUN_EXECUTION_MODAL_TITLE';
    const runExecutionModalMessage = 'EXECUTIONS.RUN_EXECUTION_MODAL_MESSAGE';


    this._translate.get([
      runExecutionModalHeader,
      runExecutionModalOkButton,
      runExecutionModalTitle,
      runExecutionModalMessage
    ]).subscribe(
      (value: { [key: string]: string }) => {
        this.runExecutionModalHeader = value[runExecutionModalHeader];
        this.runExecutionModalOkButton = value[runExecutionModalOkButton];
        this.runExecutionModalTitle = value[runExecutionModalTitle];
        this.runExecutionModalMessage = value[runExecutionModalMessage];
      });
  }
  ngOnInit(): void {
    if (this.isArchivedPage) {
      this.statuses = this.statuses.filter(status => status.value !== 'Running');
    }
  }

  selectFilter(event: any, filter: string) {
    event.stopPropagation();
    this.showedFilter = filter;
  }

  public getDropdownItem(list: StDropDownMenuItem[], value) {
    return list.find(item => item.value === value);
  }

  @HostListener('document:click', ['$event'])
  onClick() {
    this.showedFilter = '';
  }

  public confirmRunExecution(selectedExecution) {
    this._confirmRunExecution(this.runExecutionModalOkButton, () => {
      this.onReRunExecution.emit(selectedExecution);
    });
  }

  private _confirmRunExecution(textOkButton, callback) {
    const buttons: StModalButton[] = [
      { label: 'Cancel', classes: 'button-secondary-gray', responseValue: StModalResponse.NO },
      { label: textOkButton, classes: 'button-primary', responseValue: StModalResponse.YES, closeOnClick: true }
    ];
    this._modalSubscription = this._modalService.show({
      messageTitle: this.runExecutionModalTitle,
      modalTitle: this.runExecutionModalHeader,
      buttons: buttons,
      maxWidth: 500,
      message: this.runExecutionModalMessage,
    }).pipe(take(1)).subscribe((response: any) => {
      if (response === 1) {
        this._modalService.close();
        this._modalSubscription.unsubscribe();
      } else if (response === 0) {
        this._modalSubscription.unsubscribe();
        callback.call(this);
      }
    });
  }
}
