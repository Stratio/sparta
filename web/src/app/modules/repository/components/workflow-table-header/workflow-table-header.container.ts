/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
  OnDestroy
} from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Subscription, Observable } from 'rxjs';
import { Router } from '@angular/router';

import * as workflowActions from './../../actions/workflow-list';
import { State, getSearchQuery, getCurrentGroupLevel, getBlockRunButtonState, getSelectedVersionsData } from './../../reducers';

import { DEFAULT_FOLDER, FOLDER_SEPARATOR } from './../../workflow.constants';
import { WorkflowBreadcrumbItem } from './workflow-breadcrumb/workflow-breadcrumb.model';


@Component({
  selector: 'workflow-table-header-container',
  template: `
        <workflow-table-header [selectedWorkflows]="selectedWorkflows"
            [selectedVersions]="selectedVersions"
            [selectedVersionsData]="selectedVersionsData$ | async"
            [selectedGroupsList]="selectedGroupsList"
            [levelOptions]="levelOptions"
            [searchValue]="searchValue$ | async"
            [versionsListMode]="versionsListMode"
            [blockRunButton]="blockRunButton$ | async"
            (onSearch)="onSearch($event)"
            (onDeleteWorkflows)="deleteWorkflows()"
            (downloadWorkflows)="downloadWorkflows()"
            (showExecutionConfig)="showExecutionConfig($event)"
            (onEditVersion)="editVersion($event)"
            (changeFolder)="changeFolder($event)"
            (onSimpleRun)="simpleRun($event)"
            (onDeleteVersions)="deleteVersions()"></workflow-table-header>
    `,
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowTableHeaderContainer implements OnInit, OnDestroy {

  @Input() selectedWorkflows: Array<string> = [];
  @Input() selectedVersions: Array<string> = [];
  @Input() selectedGroupsList: Array<string> = [];
  @Input() versionsListMode: boolean;

  public searchValue$: Observable<string>;
  public blockRunButton$: Observable<boolean>;
  public selectedVersionsData$: Observable<any>;

  public levelOptions: Array<WorkflowBreadcrumbItem> = [];
  private _currentLevelSubscription: Subscription;

  constructor(private _store: Store<State>,
    private route: Router,
    private _cd: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.searchValue$ = this._store.pipe(select(getSearchQuery));
    this.blockRunButton$ = this._store.pipe(select(getBlockRunButtonState));
    this.selectedVersionsData$ = this._store.pipe(select(getSelectedVersionsData));

    this._currentLevelSubscription = this._store.pipe(select(getCurrentGroupLevel)).subscribe((levelGroup: any) => {
      const level = levelGroup.group;
      const levelOptions = [{
        icon: 'icon-home',
        label: ''
      }];

      let levels = [];
      if (level.name === DEFAULT_FOLDER) {
        levels = levelOptions; // set default folder as current directory
      } else {
        levels = levelOptions.concat(level.name.split(FOLDER_SEPARATOR).slice(2)
          .map(option => ({
            icon: '',
            label: option
          })));
      }
      this.levelOptions = levelGroup.workflow && levelGroup.workflow.length ? [...levels, {
        icon: '',
        label: levelGroup.workflow
      }] : levels;
      this._cd.markForCheck();
    });
  }

  changeFolder(position: number) {
    if (position + 1 === this.levelOptions.length) {
      return;
    }
    const level = position === 0 ? DEFAULT_FOLDER : DEFAULT_FOLDER +
      FOLDER_SEPARATOR + this.levelOptions.slice(1, position + 1).map(option => option.label).join(FOLDER_SEPARATOR);
    this._store.dispatch(new workflowActions.ChangeGroupLevelAction(level));
  }

  onSearch(event: string) {
    this._store.dispatch(new workflowActions.SearchCurrentFolderAction(event));
  }

  editVersion(versionId: string) {
    this.route.navigate(['wizard/edit', this.selectedVersions[0]]);
  }

  deleteWorkflows(): void {
    this._store.dispatch(new workflowActions.DeleteWorkflowAction());
  }

  downloadWorkflows(): void {
    this._store.dispatch(new workflowActions.DownloadWorkflowsAction(this.selectedVersions));
  }

  deleteVersions(): void {
    this._store.dispatch(new workflowActions.DeleteVersionAction());
  }

  simpleRun(event) {
    this._store.dispatch(new workflowActions.RunWorkflowAction(event));
  }

  showExecutionConfig({id, schedule}: any) {
    this._store.dispatch(new workflowActions.ConfigAdvancedExecutionAction({workflowId:id, schedule}));
  }

  ngOnDestroy() {
    this._currentLevelSubscription && this._currentLevelSubscription.unsubscribe();
  }

}
