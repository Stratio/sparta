/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
  ViewContainerRef,
  Input,
  Output,
  EventEmitter
} from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Router, ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { StModalService, StHorizontalTab } from '@stratio/egeo';

import { State } from './reducers';
import * as executionsActions from './actions/executions';
import * as fromRoot from './reducers';

@Component({
  selector: 'executions-list',
  styleUrls: ['executions-list.component.scss'],
  templateUrl: 'executions-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExecutionsListComponent implements OnInit, OnDestroy {

  @ViewChild('newExecutionsModalMonitoring', { read: ViewContainerRef })
  target: any;

  @Input() showSidebar: boolean;
  @Input() isArchivedPage: boolean;
  @Output() closeSidebar = new EventEmitter<void>();

  public selectedExecutions: Array<any> = [];
  public selectedExecutionsIds: string[] = [];
  public executionInfo: any;
  public showInitialMode: Observable<boolean>;
  public executionsList$: Observable<any>;
  public isLoading$: Observable<boolean>;
  public selectedExecution: any;
  public showDebugConsole = false;
  public isEmptyList: boolean;
  public isEmptyFilter: boolean;


  public sidebarPosition = 0;

  private _componentDestroyed = new Subject();
  private _intervalHandler;

  constructor(
    private _store: Store<State>,
    private _route: Router,
    private _activatedRoute: ActivatedRoute,
    private _cd: ChangeDetectorRef,
    private _modalService: StModalService
  ) {

  }

  ngOnInit() {
    this._store.dispatch(
      new executionsActions.SetArchivedPageAction(this.isArchivedPage)
    );
    if (this.isArchivedPage) {
      this._store.dispatch(
        new executionsActions.ListArchivedExecutionsAction()
      );
    } else {
      this._store.dispatch(new executionsActions.ListExecutionsAction());
    }
    this._modalService.container = this.target;

    this.executionsList$ = this._store.pipe(
      select(fromRoot.getFilteredSearchExecutionsList)
    );
    this.isLoading$ = this._store.pipe(select(fromRoot.getIsLoading));

    this._store
      .pipe(select(fromRoot.isEmptyList))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((isEmptyList: boolean) => {
        this.isEmptyList = isEmptyList;
        this._cd.markForCheck();
      });

    this._store
      .pipe(select(fromRoot.getSelectedExecutions))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((selectedIds: any) => {
        this.selectedExecutionsIds = selectedIds;
        this._cd.markForCheck();
      });

    this._store
      .pipe(select(fromRoot.getLastSelectedExecution))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(selectedExecution => {
        this.selectedExecution = selectedExecution;
        this._cd.markForCheck();
      });

    this._store
      .pipe(select(fromRoot.getExecutionInfo))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(executionInfo => {
        this.executionInfo = executionInfo;
        this._cd.markForCheck();
      });

    this._store
      .pipe(select(fromRoot.isEmptyFilter))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((isEmptyFilter: boolean) => {
        this.isEmptyFilter = isEmptyFilter;
        this._cd.markForCheck();
      });
  }


  showWorkflowExecutionInfo(workflowEvent) {
    this._store.dispatch(
      new executionsActions.GetExecutionInfoAction({
        id: workflowEvent.id,
        name: workflowEvent.name
      })
    );
  }

  showConsole(ev) {
    this.showDebugConsole = true;
  }

  onCloseConsole() {
    this.showDebugConsole = false;
  }

  closeExecutionInfo() {
    this._store.dispatch(
      new executionsActions.CloseWorkflowExecutionInfoAction()
    );
  }

  goToRepository() {
    this._route.navigate(['repository']);
  }

  ngOnDestroy() {
    clearInterval(this._intervalHandler);
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
    this._store.dispatch(new executionsActions.CancelExecutionPolling());
    this._store.dispatch(new executionsActions.ResetValuesAction());
  }
}
