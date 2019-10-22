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
  OnInit
} from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Order } from '@stratio/egeo';
import { Observable, Subscription } from 'rxjs';

import * as workflowActions from './../../actions/workflow-list';
import { State, getVersionsOrderedList, getCurrentGroupLevel } from './../../reducers';
import { Group } from '../../models/workflows';
import {CITags} from '@models/enums';


@Component({
  selector: 'repository-table-container',
  template: `
        <repository-table [workflowList]="workflowList"
            [workflowVersions]="workflowVersions$ | async"
            [selectedGroupsList]="selectedGroupsList"
            [selectedWorkflows]="selectedWorkflows"
            [selectedVersions]="selectedVersions"
            [previousLevel]="previousLevel"
            [groupList]="groupList"
            (onChangeOrder)="changeOrder($event)"
            (onChangeOrderVersions)="changeOrderVersions($event)"
            (changeFolder)="changeFolder($event)"
            (openWorkflow)="showWorkflowVersions($event)"
            (onDeleteVersion)="onDeleteVersion($event)"
            (selectWorkflow)="selectWorkflow($event)"
            (onDeleteFolder)="onDeleteFolder($event)"
            (generateVersion)="generateVersion($event)"
            (promoteVersion)="promoteVersion($event)"
            (onDeleteWorkflow)="onDeleteWorkflow($event)"
            (selectGroup)="selectGroup($event)"
            (selectVersion)="selectVersion($event)"
            (onSimpleRun)="simpleRun($event)"
            (showExecutionConfig)="showExecutionConfig($event)"
            (duplicateWorkflow)="onDuplicateWorkflow($event)"></repository-table>
    `,
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class RepositoryTableContainer implements OnInit {

  @Input() selectedWorkflows: Array<string> = [];
  @Input() selectedGroupsList: Array<string> = [];
  @Input() workflowList: Array<any> = [];
  @Input() groupList: Array<any> = [];
  @Input() selectedVersions: Array<string> = [];
  @Input() workflowVersions: Array<any> = [];

  @Output() showWorkflowInfo = new EventEmitter<void>();
  @Output() showExecution = new EventEmitter<any>();

  public previousLevel: any;
  public workflowVersions$: Observable<Array<any>>;
  private _currentLevelSubscription: Subscription;

  ngOnInit(): void {
    this.workflowVersions$ = this._store.pipe(select(getVersionsOrderedList));
    this._currentLevelSubscription = this._store.pipe(select(getCurrentGroupLevel)).subscribe((currentLevel: any) => {
      const levelSplitted = currentLevel.group.name.split('/');
      if (currentLevel.workflow.length) {
        this.previousLevel = {
          label: currentLevel.group.label,
          value: currentLevel.group.name
        };
      } else if (levelSplitted.length < 3) {
        this.previousLevel = null;
      } else {
        this.previousLevel = {
          label: levelSplitted[levelSplitted.length - 2],
          value: levelSplitted.slice(0, levelSplitted.length - 1).join('/')
        };
      }

    });
  }

  changeOrder(event: Order) {
    this._store.dispatch(new workflowActions.ChangeOrderAction(event));
  }

  changeOrderVersions(event: Order) {
    this._store.dispatch(new workflowActions.ChangeVersionsOrderAction(event));
  }

  selectWorkflow(name: string) {
    this._store.dispatch(new workflowActions.SelectWorkflowAction(name));
  }

  selectGroup(name: string) {
    this._store.dispatch(new workflowActions.SelectGroupAction(name));
  }

  selectVersion(id: string) {
    this._store.dispatch(new workflowActions.SelectVersionAction(id));
  }

  changeFolder(event: Group) {
    this._store.dispatch(new workflowActions.ChangeGroupLevelAction(event));
  }

  showWorkflowVersions(workflow: any) {
    this._store.dispatch(new workflowActions.ShowWorkflowVersionsAction({
      name: workflow.name,
      group: workflow.group
    }));
  }

  onDeleteFolder(folderId: string) {
    this._store.dispatch(new workflowActions.DeleteSingleGroupAction(folderId));
  }

  onDeleteWorkflow(workflowName: string) {
    this._store.dispatch(new workflowActions.DeleteSingleWorkflowAction(workflowName));
  }

  onDeleteVersion(versionId: string) {
    this._store.dispatch(new workflowActions.DeleteSingleVersionAction(versionId));
  }

  generateVersion(versionId: string): void {
    this._store.dispatch(new workflowActions.GenerateNewVersionAction(versionId));
  }

  promoteVersion(version): void {
    if (version.ciCdLabel === CITags.ReleaseCandidate) {
      this._store.dispatch(new workflowActions.PromoteReleaseAction(version.id));
    } else if (!version.ciCdLabel || (version.ciCdLabel !== CITags.ReleaseCandidate
                                      || version.ciCdLabel !== CITags.Released)) {
      this._store.dispatch(new workflowActions.PromoteCandidateAction(version.id));
    }
  }

  showExecutionConfig(event: any) {
    this.showExecution.emit(event);
  }

  simpleRun(version: any) {
    this._store.dispatch(new workflowActions.RunWorkflowAction(version));
  }

  public onDuplicateWorkflow(version) {
    this._store.dispatch(new workflowActions.DuplicateWorkflowAction({
      id: version.id,
      name: `${version.name}-copy`,
      group: version.group.name,
      tag: version.version
    }));
  }

  constructor(private _store: Store<State>) { }

}
