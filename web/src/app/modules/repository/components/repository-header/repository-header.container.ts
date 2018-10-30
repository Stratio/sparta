/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';

import * as workflowActions from './../../actions/workflow-list';
import { State,  getVersionsOrderedList, getSelectedVersionsData, getNotificationMessage, getBlockRunButtonState } from './../../reducers';


@Component({
    selector: 'repository-header-container',
    template: `
        <repository-header [selectedWorkflows]="selectedWorkflows"
            [selectedVersions]="selectedVersions"
            [selectedVersionsData]="selectedVersionsData$ | async"
            [selectedGroupsList]="selectedGroupsList"
            [versionsListMode]="versionsListMode"
            [notificationMessage]="notificationMessage$ | async"
            [showDetails]="showDetails"
            [blockRunButton]="blockRunButton$ | async"
            (onEditVersion)="editVersion($event)"
            (downloadWorkflows)="downloadWorkflows()"
            (showWorkflowInfo)="showWorkflowInfo.emit()"
            (onDeleteWorkflows)="deleteWorkflows()"
            (hideNotification)="hideNotification()"
            (showExecutionConfig)="showExecutionConfig($event)"
            (onSimpleRun)="simpleRun($event)"
            (onDeleteVersions)="deleteVersions()"></repository-header>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class RepositoryHeaderContainer implements OnInit, OnDestroy {

    @Input() selectedWorkflows: Array<string> = [];
    @Input() selectedVersions: Array<string> = [];
    @Input() selectedGroupsList: Array<string> = [];
    @Input() workflowStatuses: any = {};
    @Input() showDetails: boolean;
    @Input() versionsListMode: boolean;

    @Output() showWorkflowInfo = new EventEmitter<void>();

    public levelOptions: Array<string> = [];
    public openedWorkflow = '';
    public workflowVersions$: Observable<any>;
    public selectedVersionsData$: Observable<any>;
    public notificationMessage$: Observable<any>;
    public blockRunButton$: Observable<boolean>;

    private openedWorkflowSubscription: Subscription;
    private currentLevelSubscription: Subscription;

    ngOnInit(): void {
        this.selectedVersionsData$ = this._store.select(getSelectedVersionsData);
        this.workflowVersions$ = this._store.select(getVersionsOrderedList);
        this.notificationMessage$ = this._store.select(getNotificationMessage);
        this.blockRunButton$ = this._store.select(getBlockRunButtonState);
    }

    constructor(private _store: Store<State>, private _cd: ChangeDetectorRef, private route: Router) { }

    editVersion(versionId: string) {
        this.route.navigate(['wizard/edit', this.selectedVersions[0]]);
    }

    downloadWorkflows(): void {
        this._store.dispatch(new workflowActions.DownloadWorkflowsAction(this.selectedVersions));
    }

    selectGroup(group: any) {
        this._store.dispatch(new workflowActions.ChangeGroupLevelAction(group));
    }

    deleteWorkflows(): void {
        this._store.dispatch(new workflowActions.DeleteWorkflowAction());
    }

    deleteVersions(): void {
        this._store.dispatch(new workflowActions.DeleteVersionAction());
    }

    showExecutionConfig(id: string) {
        this._store.dispatch(new workflowActions.ConfigAdvancedExecutionAction(id));
    }

    hideNotification(): void {
        this._store.dispatch(new workflowActions.HideNotificationAction());
    }

    simpleRun(event) {
        this._store.dispatch(new workflowActions.RunWorkflowAction(event));
    }

    ngOnDestroy(): void {
        this.currentLevelSubscription && this.currentLevelSubscription.unsubscribe();
        this.openedWorkflowSubscription && this.openedWorkflowSubscription.unsubscribe();
    }

}
