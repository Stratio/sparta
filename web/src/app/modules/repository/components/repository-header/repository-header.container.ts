/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { Store, select } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';

import * as workflowActions from './../../actions/workflow-list';
import { State, getNotificationMessage } from './../../reducers';

@Component({
  selector: 'repository-header-container',
  template: `
        <repository-header
            [notificationMessage]="notificationMessage$ | async"
            [showDetails]="showDetails"
            (showWorkflowInfo)="showWorkflowInfo.emit()"
            (hideNotification)="hideNotification()"></repository-header>
    `,
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class RepositoryHeaderContainer implements OnInit {

  @Input() workflowStatuses: any = {};
  @Input() showDetails: boolean;
  @Input() versionsListMode: boolean;

  @Output() showWorkflowInfo = new EventEmitter<void>();

  public levelOptions: Array<string> = [];
  public notificationMessage$: Observable<any>;

  constructor(private _store: Store<State>,
    private _cd: ChangeDetectorRef,
    private route: Router) { }

  ngOnInit(): void {
    this.notificationMessage$ = this._store.pipe(select(getNotificationMessage));
  }

  selectGroup(group: any) {
    this._store.dispatch(new workflowActions.ChangeGroupLevelAction(group));
  }

  hideNotification(): void {
    this._store.dispatch(new workflowActions.HideNotificationAction());
  }

}
