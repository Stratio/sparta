/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs/Rx';

import * as userActions from 'actions/user';
import * as workflowActions from './../../actions/workflow-list';
import { State, getCurrentGroupLevel, getVersionsOrderedList, getSelectedVersionsData } from './../../reducers';
import { DEFAULT_FOLDER, FOLDER_SEPARATOR } from './../../workflow.constants';
import { Router } from '@angular/router';


@Component({
    selector: 'workflows-manage-header-container',
    template: `
        <workflows-manage-header [selectedWorkflows]="selectedWorkflows"
            [selectedVersions]="selectedVersions"
            [selectedVersionsData]="selectedVersionsData$ | async"
            [selectedGroupsList]="selectedGroupsList"
            [versionsListMode]="versionsListMode"
            [showDetails]="showDetails"
            [levelOptions]="levelOptions"
            (onEditVersion)="editVersion($event)"
            (generateVersion)="generateVersion()"
            (downloadWorkflows)="downloadWorkflows()" 
            (showWorkflowInfo)="showWorkflowInfo.emit()"
            (onDeleteWorkflows)="deleteWorkflows($event)"
            (onDeleteVersions)="deleteVersions()"
            (changeFolder)="changeFolder($event)"></workflows-manage-header>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsManagingHeaderContainer implements OnInit, OnDestroy {

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
    private openedWorkflowSubscription: Subscription;
    private currentLevelSubscription: Subscription;

    ngOnInit(): void {
        this.selectedVersionsData$ = this._store.select(getSelectedVersionsData);
        this.workflowVersions$ = this._store.select(getVersionsOrderedList);
        this.currentLevelSubscription = this._store.select(getCurrentGroupLevel).subscribe((levelGroup: any) => {
            const level = levelGroup.group;
            const levelOptions = ['Home'];

            let levels = [];
            if (level.name === DEFAULT_FOLDER) {
                levels = levelOptions;
            } else {
                levels = levelOptions.concat(level.name.split(FOLDER_SEPARATOR).slice(2));
            }
            this.levelOptions = levelGroup.workflow && levelGroup.workflow.length ? [...levels, levelGroup.workflow] : levels;
            this._cd.detectChanges();
        });
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

    deleteWorkflows(workflows: Array<any>): void {
        this._store.dispatch(new workflowActions.DeleteWorkflowAction(workflows));
    }

    deleteVersions(): void {
        this._store.dispatch(new workflowActions.DeleteVersionAction());
    }

    changeFolder(position: number) {
        const level = position === 0 ? DEFAULT_FOLDER : DEFAULT_FOLDER +
            FOLDER_SEPARATOR + this.levelOptions.slice(1, position + 1).join(FOLDER_SEPARATOR);
        this._store.dispatch(new workflowActions.ChangeGroupLevelAction(level));
    }

    generateVersion(): void {
        this._store.dispatch(new workflowActions.GenerateNewVersionAction());
    }

    ngOnDestroy(): void {
        this.currentLevelSubscription && this.currentLevelSubscription.unsubscribe();
        this.openedWorkflowSubscription && this.openedWorkflowSubscription.unsubscribe();
    }

}
