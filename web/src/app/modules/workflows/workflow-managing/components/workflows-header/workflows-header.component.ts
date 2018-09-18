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
   OnChanges,
   OnDestroy,
   Output,
   SimpleChanges,
   ViewChild,
   ViewContainerRef
} from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { StModalService, StModalResponse, StModalButton } from '@stratio/egeo';
import { Subscription } from 'rxjs/Subscription';

import { WorkflowsManagingService } from './../../workflows.service';
import { DuplicateWorkflowComponent } from './../duplicate-workflow-modal/duplicate-workflow.component';
import { isWorkflowRunning } from '@utils';
import { MenuOptionListGroup } from '@app/shared/components/menu-options-list/menu-options-list.component';

@Component({
   selector: 'workflows-manage-header',
   styleUrls: ['workflows-header.component.scss'],
   templateUrl: 'workflows-header.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsManagingHeaderComponent implements OnChanges, OnDestroy {
   @ViewChild('newWorkflowModal', { read: ViewContainerRef }) target: any;

   @Input() selectedWorkflows: Array<string>;
   @Input() selectedVersions: Array<string>;
   @Input() selectedVersionsData: Array<any>;
   @Input() selectedGroupsList: Array<string>;
   @Input() showDetails = false;
   @Input() levelOptions: Array<string>;
   @Input() notificationMessage: any;
   @Input() versionsListMode = false;

   @Output() downloadWorkflows = new EventEmitter();
   @Output() showWorkflowInfo = new EventEmitter();
   @Output() onDeleteWorkflows = new EventEmitter();
   @Output() onDeleteVersions = new EventEmitter();
   @Output() changeFolder = new EventEmitter();
   @Output() generateVersion = new EventEmitter();
   @Output() onEditVersion = new EventEmitter<any>();
   @Output() hideNotification = new EventEmitter();
   @Output() showExecutionConfig = new EventEmitter<string>();
   @Output() onSimpleRun = new EventEmitter<any>();

   public selectedVersionsInner: Array<string> = [];
   public selectedWorkflowsInner: Array<string> = [];
   public selectedGroupsListInner: Array<string> = [];
   public visibleNotification = true;
   public runOptions: MenuOptionListGroup[] = [
      {
         options: [
            {
               label: 'Run',
               id: 'simple'
            },
            {
               label: 'Run with custom params',
               id: 'advanced'
            }
         ]
      }
   ];
   public menuOptions: MenuOptionListGroup[] = [
      {
         options: [
            {
               icon: 'icon-folder',
               label: 'New folder',
               id: 'group'
            }
         ]
      },
      {
         options: [
            {
               icon: 'icon-streaming-workflow',
               label: 'Streaming workflow',
               id: 'streaming'
            },
            {
               icon: 'icon-batch-workflow',
               label: 'Batch workflow',
               id: 'batch'
            }
         ]
      },
      {
         options: [
            {
               icon: 'icon-json',
               label: 'Import from JSON',
               id: 'file'
            }
         ]
      },
   ];
   public isRunning = isWorkflowRunning;
   public deleteWorkflowModalTitle: string;
   public deleteModalTitle: string;
   public deleteWorkflowModalMessage: string;
   public messageDeleteTitle: string;
   public duplicateWorkflowTitle: string;

   public renameFolderTitle: string;
   public renameWorkflowTitle: string;

   public moveGroupTitle: string;

   public menuOptionsDuplicate: any = [
      {
         name: 'Generate new version',
         value: 'version'
      }, {
         name: 'New workflow from this version',
         value: 'workflow'
      }
   ];

   private _modalSubscription: Subscription;
   private _notificationHandler;

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

   public editVersion(): void {
      this.onEditVersion.emit(this.selectedVersions[0]);
   }

   public changeNotificationVisibility(visible: boolean) {
      if (!visible) {
         this.hideNotification.emit();
      } else {
         clearInterval(this._notificationHandler);
         this._notificationHandler = setTimeout(() => this.hideNotification.emit(), 5000);
      }
   }

   public selectLevel(event: number): void {
      this.changeFolder.emit(event);
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
      }).take(1).subscribe((response: any) => {
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
      if (event === 'simple') {
         this.simpleRun();
      } else {
         this.showExecutionConfig.emit(this.selectedVersions[0]);
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
