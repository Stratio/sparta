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
  ViewContainerRef
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
  selector: 'sparta-managing-executions',
  styleUrls: ['executions.styles.scss'],
  templateUrl: 'executions.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExecutionsManagingComponent implements OnInit, OnDestroy {

  @ViewChild('newExecutionsModalMonitoring', { read: ViewContainerRef })
  target: any;

  public showDetails = false;

  public selectedExecutions: Array<any> = [];
  public selectedExecutionsIds: string[] = [];
  public executionInfo: any;
  public showInitialMode: Observable<boolean>;
  public executionsList$: Observable<any>;
  public isLoading$: Observable<boolean>;
  public selectedExecution: any;
  public showDebugConsole = false;
  public isArchivedPage = false;
  public isEmptyList: boolean;
  public isEmptyFilter: boolean;
  public fixSubHeaders = false;
  public executionsTabs: StHorizontalTab[] = [
    {
      id: 'executions',
      text: 'Executions'
    },
    {
      id: 'scheduled',
      text: 'Scheduled'
    }
  ];
  public selectedTab = this.executionsTabs[0];

  public sidebarPosition = 0;
  public hiddenToolBar = false;

  private _componentDestroyed = new Subject();
  private _lastPosition = 0;
  private _intervalHandler;

  constructor(
    private _store: Store<State>,
    private _route: Router,
    private _activatedRoute: ActivatedRoute,
    private _cd: ChangeDetectorRef,
    private _modalService: StModalService
  ) {
    this._onScroll = this._onScroll.bind(this);
    document.addEventListener('scroll', this._onScroll);
  }

  ngOnInit() {
    this._modalService.container = this.target;

    this._activatedRoute.pathFromRoot[
      this._activatedRoute.pathFromRoot.length - 1
    ].data
      .pipe(take(1))
      .subscribe(v => {
        this.isArchivedPage = v.archived ? true : false;
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
      });
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

  onShowExecutionInfo() {
    this.showDetails = !this.showDetails;
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

  changedOption(event: StHorizontalTab) {
    this.selectedTab = event;
    if(event.id === 'scheduled') {
      this._store.dispatch(new executionsActions.ChangeExecutionsDeselectAllExecutions());
    }
  }

  goToRepository() {
    this._route.navigate(['repository']);
  }

  private _onScroll(event) {
    const top = window.pageYOffset || document.documentElement.scrollTop;
    this.hiddenToolBar = top > 50 && this._lastPosition < top;
    this.fixSubHeaders = top > 90;
    this._lastPosition = top;
    this._cd.markForCheck();
  }

  ngOnDestroy() {
    clearInterval(this._intervalHandler);
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
    this._store.dispatch(new executionsActions.CancelExecutionPolling());
    this._store.dispatch(new executionsActions.ResetValuesAction());
  }
}
