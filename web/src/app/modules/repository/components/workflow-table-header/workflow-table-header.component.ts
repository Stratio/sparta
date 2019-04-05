/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, ViewChild, ViewContainerRef } from '@angular/core';
import { StModalService, StModalResponse, StModalButton } from '@stratio/egeo';
import { Subscription } from 'rxjs';
import { take } from 'rxjs/operators';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

import { DuplicateWorkflowComponent } from './../duplicate-workflow-modal/duplicate-workflow.component';
import { WorkflowsManagingService } from './../../workflows.service';
import { WorkflowBreadcrumbItem } from './workflow-breadcrumb/workflow-breadcrumb.model';
import { MenuOptionListGroup } from '@app/shared/components/menu-options-list/menu-options-list.component';
import { MENU_OPTIONS, RUN_OPTIONS, MENU_OPTIONS_DUPLICATE } from './workflow-table-header-menus.model';
import { isWorkflowRunning } from '@utils';

@Component({
  selector: 'workflow-table-header',
  styleUrls: ['workflow-table-header.component.scss'],
  templateUrl: 'workflow-table-header.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowTableHeaderComponent implements OnChanges, OnDestroy {

  @Input() selectedWorkflows: Array<string>;
  @Input() selectedVersions: Array<string>;
  @Input() selectedVersionsData: Array<any>;
  @Input() selectedGroupsList: Array<string>;
  @Input() versionsListMode = false;

  @Input() levelOptions: Array<WorkflowBreadcrumbItem>;
  @Input() searchValue: string;
  @Input() blockRunButton: boolean;

  @Output() changeFolder = new EventEmitter<number>();
  @Output() onSearch = new EventEmitter<string>();
  @Output() downloadWorkflows = new EventEmitter();
  @Output() onDeleteWorkflows = new EventEmitter();
  @Output() onDeleteVersions = new EventEmitter();
  @Output() generateVersion = new EventEmitter();
  @Output() onEditVersion = new EventEmitter<any>();
  @Output() showExecutionConfig = new EventEmitter<any>();
  @Output() onSimpleRun = new EventEmitter<any>();

  @ViewChild('newWorkflowModal', { read: ViewContainerRef }) target: any;

  public selectedVersionsInner: Array<string> = [];
  public selectedWorkflowsInner: Array<string> = [];
  public selectedGroupsListInner: Array<string> = [];

  public isRunning = isWorkflowRunning;
  public deleteWorkflowModalTitle: string;
  public deleteModalTitle: string;
  public deleteWorkflowModalMessage: string;
  public messageDeleteTitle: string;
  public duplicateWorkflowTitle: string;

  public renameFolderTitle: string;
  public renameWorkflowTitle: string;

  public moveGroupTitle: string;
  public runOptions: MenuOptionListGroup[] = RUN_OPTIONS;
  public menuOptions: MenuOptionListGroup[] = MENU_OPTIONS;
  public menuOptionsDuplicate: any = MENU_OPTIONS_DUPLICATE;

  private _modalSubscription: Subscription;

  constructor(private _modalService: StModalService,
    private translate: TranslateService,
    private route: Router,
    public workflowsService: WorkflowsManagingService) {

    const deleteWorkflowModalTitle = 'DASHBOARD.DELETE_WORKFLOW_TITLE';
    const deleteWorkflowModalMessage = 'DASHBOARD.DELETE_WORKFLOW_MESSAGE';
    const messageDeleteTitle = 'DASHBOARD.MESSAGE_DELETE_TITLE';
    const renameFolderTitle = 'DASHBOARD.RENAME_FOLDER_TITLE';
    const renameWorkflowTitle = 'DASHBOARD.RENAME_WORKFLOW_TITLE';
    const moveGroupTitle = 'DASHBOARD.MOVE_GROUP_TITLE';
    const deleteModalTitle = 'DASHBOARD.DELETE_TITLE';
    const duplicateWorkflowTitle = 'DASHBOARD.DUPLICATE_WORKFLOW';

    this.translate.get([deleteWorkflowModalTitle, deleteWorkflowModalMessage, messageDeleteTitle,
      renameFolderTitle, renameWorkflowTitle, moveGroupTitle, deleteModalTitle, duplicateWorkflowTitle]).subscribe(
        (value: { [key: string]: string }) => {
          this.deleteWorkflowModalTitle = value[deleteWorkflowModalTitle];
          this.deleteWorkflowModalMessage = value[deleteWorkflowModalMessage];
          this.messageDeleteTitle = value[messageDeleteTitle];
          this.renameFolderTitle = value[renameFolderTitle];
          this.renameWorkflowTitle = value[renameWorkflowTitle];
          this.moveGroupTitle = value[moveGroupTitle];
          this.deleteModalTitle = value[deleteModalTitle];
          this.duplicateWorkflowTitle = value[duplicateWorkflowTitle];
        });
  }


  ngOnChanges(changes: SimpleChanges): void {
    if (changes['selectedWorkflows']) {
      const workflowsVal = changes['selectedWorkflows'].currentValue;
      this.selectedWorkflowsInner = workflowsVal ? workflowsVal : [];
    }

    if (changes['selectedGroupsList']) {
      const groupsVal = changes['selectedGroupsList'].currentValue;
      this.selectedGroupsListInner = groupsVal ? groupsVal : [];
    }

    if (changes['selectedVersions']) {
      const versionsVal = changes['selectedVersions'].currentValue;
      this.selectedVersionsInner = versionsVal ? versionsVal : [];
    }
  }

  public editVersion(): void {
    this.onEditVersion.emit(this.selectedVersions[0]);
  }

  public deleteWorkflowConfirmModal() {
    const buttons: StModalButton[] = [
      { label: 'Cancel', classes: 'button-secondary-gray', responseValue: StModalResponse.NO },
      { label: 'Delete', classes: 'button-critical', responseValue: StModalResponse.YES, closeOnClick: true }
    ];
    this._modalSubscription = this._modalService.show({
      messageTitle: this.deleteWorkflowModalMessage,
      modalTitle: this.deleteModalTitle,
      buttons: buttons,
      maxWidth: 500,
      message: this.messageDeleteTitle,
    }).pipe(take(1)).subscribe((response: any) => {
      if (response === 1) {
        this._modalService.close();
        this._modalSubscription.unsubscribe();
      } else if (response === 0) {
        if (this.selectedVersions && this.selectedVersions.length) {
          this.onDeleteVersions.emit();
        } else {
          this.onDeleteWorkflows.emit();
        }
      }
    });
  }

  public selectedMenuOption(event: any) {
    switch (event) {
      case 'streaming':
        this.route.navigate(['wizard/streaming']);
        break;
      case 'batch':
        this.route.navigate(['wizard/batch']);
        break;
      case 'group':
        this.workflowsService.createWorkflowGroup();
        break;
      case 'file':
        this.workflowsService.showCreateJsonModal();
        break;
      default:
        this.workflowsService.showCreateJsonModal();
        break;
    }
  }

  public selectedExecution(event: string) {
    if (event === 'run-simple') {
      this.simpleRun();
    } else if (event === 'run-advanced') {
      this.showExecutionConfig.emit({id: this.selectedVersions[0]});
    } else if (event === 'schedule-simple') {
      this.workflowsService.createSchedule(this.selectedVersions[0]);
    } else if (event === 'schedule-advanced') {
      this.showExecutionConfig.emit({
        id: this.selectedVersions[0],
        schedule: true
      });
    }
  }

  public simpleRun() {
    this.onSimpleRun.emit({
      workflowId: this.selectedVersionsData[0].id,
      workflowName: this.selectedVersionsData[0].name
    });
  }

  public selectedDuplicatedOption(event: any) {
    if (event.value === 'workflow') {
      this._modalService.show({
        modalTitle: this.duplicateWorkflowTitle,
        maxWidth: 500,
        inputs: {
          version: this.selectedVersionsData[0]
        },
        outputs: {
          onCloseDuplicateModal: (response: any) => {
            this._modalService.close();
          }
        },
      }, DuplicateWorkflowComponent);
    } else {
      this.generateVersion.emit();
    }
  }

  ngOnDestroy(): void {
    if (this._modalSubscription) {
      this._modalSubscription.unsubscribe();
    }
  }

}
