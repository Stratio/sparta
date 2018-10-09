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
   ChangeDetectorRef
} from '@angular/core';
import { StTableHeader, Order, StModalService, StModalButton, StModalResponse } from '@stratio/egeo';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs/Rx';

import { Group } from '../../models/workflows';
import { MenuOptionListGroup } from '@app/shared/components/menu-options-list/menu-options-list.component';
import { WorkflowRenameModal } from '@app/workflows/workflow-managing/components/workflow-rename-modal/workflow-rename.component';
import { MoveGroupModal } from '@app/workflows/workflow-managing/components/move-group-modal/move-group.component';
import { DuplicateWorkflowComponent } from '@app/workflows/workflow-managing/components/duplicate-workflow-modal/duplicate-workflow.component';
@Component({
   selector: 'workflows-manage-table',
   styleUrls: ['workflows-table.component.scss'],
   templateUrl: 'workflows-table.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsManagingTableComponent {

   @Input() workflowList: Array<any> = [];
   @Input() groupList: Array<any> = [];
   @Input() selectedWorkflows: Array<string> = [];
   @Input() selectedGroupsList: Array<string> = [];
   @Input() selectedVersions: Array<string> = [];
   @Input() workflowVersions: Array<any> = [];
   @Input() previousLevel: any;

   @Output() onChangeOrder = new EventEmitter<Order>();
   @Output() onChangeOrderVersions = new EventEmitter<Order>();
   @Output() selectWorkflow = new EventEmitter<string>();
   @Output() selectGroup = new EventEmitter<string>();
   @Output() selectVersion = new EventEmitter<string>();
   @Output() openWorkflow = new EventEmitter<any>();
   @Output() changeFolder = new EventEmitter<any>();

   @Output() onDeleteVersion = new EventEmitter();
   @Output() onDeleteWorkflow = new EventEmitter();
   @Output() onDeleteFolder = new EventEmitter<string>();

   @Output() generateVersion = new EventEmitter<string>();

   @Output() showExecutionConfig = new EventEmitter<string>();
   @Output() onSimpleRun = new EventEmitter<any>();

   /* modal titles */
   public deleteWorkflowModalTitle: string;
   public deleteVersionModalTitle: string;
   public deleteFolderModalTitle: string;

   public deleteModalTitle: string;
   public deleteWorkflowModalMessage: string;
   public messageDeleteTitle: string;
   public duplicateWorkflowTitle: string;

   public renameFolderTitle: string;
   public renameWorkflowTitle: string;

   public moveGroupTitle: string;

   public fields: StTableHeader[];
   public versionFields: StTableHeader[];

   public openWorkflows: Array<string> = [];
   public groupOptions: MenuOptionListGroup[] = [
      {
         options: [
            {
               icon: 'icon-edit',
               label: 'Rename',
               id: 'group-name-edition'
            },
            {
               icon: 'icon-move-to-folder',
               label: 'Move',
               id: 'group-move'
            }
         ]
      },
      {
         options: [
            {
               icon: 'icon-trash',
               label: 'Delete',
               id: 'group-delete',
               color: 'critical'
            }
         ]
      }
   ];
   public workflowOptions: MenuOptionListGroup[] = [
      {
         options: [
            {
               icon: 'icon-edit',
               label: 'Rename',
               id: 'workflow-name-edition'
            },
            {
               icon: 'icon-move-to-folder',
               label: 'Move',
               id: 'workflow-move'
            }
         ]
      },
      {
         options: [
            {
               icon: 'icon-trash',
               label: 'Delete',
               id: 'workflow-delete',
               color: 'critical'
            }
         ]
      }
   ];

   public versionOptions: MenuOptionListGroup[] = [
      {
         options: [{
            icon: 'icon-edit-3',
            label: 'Edit',
            id: 'version-edit'
         }]
      },
      {

         options: [
            {
               icon: 'icon-play',
               label: 'Run',
               id: 'version-run-workflow'
            },
            {
               icon: 'icon-play',
               label: 'Run with parameters',
               id: 'version-run-params-workflow'
            }]
      }, {
         options: [
            {
               icon: 'icon-square-plus',
               label: 'New workflow from this version',
               id: 'version-new-workflow'
            },
            {
               icon: 'icon-square-plus',
               label: 'New version',
               id: 'version-new-version'
            },

         ]
      },
      {
         options: [
            {
               icon: 'icon-trash',
               label: 'Delete',
               id: 'version-delete',
               color: 'critical'
            }
         ]
      }
   ];

   private _modalSubscription: Subscription;

   changeOrder(event: Order): void {
      this.onChangeOrder.emit(event);
   }

   changeOrderVersions(event: Order): void {
      this.onChangeOrderVersions.emit(event);
   }

   checkVersion(id: string) {
      this.selectVersion.emit(id);
   }

   checkWorkflow(workflow: any) {
      this.selectWorkflow.emit(workflow.name);
   }

   checkGroup(group: Group) {
      this.selectGroup.emit(group.name);
   }

   openWorkflowClick(event: Event, workflow: any) {
      event.stopPropagation();
      this.scrollToTop();
      this.openWorkflow.emit(workflow);
   }

   openGroup(event: Event, group: Group) {
      event.stopPropagation();
      this.scrollToTop();
      this.changeFolder.emit(group);
   }

   showSparkUI(url: string) {
      window.open(url, '_blank');
   }

   editVersion(event: Event, versionId: string) {
      event.stopPropagation();
      this.route.navigate(['wizard/edit', versionId]);
   }

   selectGroupAction(event: string, group: Group) {
      switch (event) {
         case 'group-name-edition':
            this._showNameEditionModal(this.renameFolderTitle, 'Group', group.name);
            break;
         case 'group-move':
            this._moveTo(null, group.name);
            break;
         case 'group-delete':
            this._deleteConfirmModal(this.deleteFolderModalTitle, () => this.onDeleteFolder.emit(group.id));
            break;
      }
   }

   selectedWorkflowAction(event: string, workflow: any) {
      switch (event) {
         case 'workflow-name-edition':
            this._showNameEditionModal(this.renameFolderTitle, 'Workflow', workflow.name);
            break;
         case 'workflow-move':
            this._moveTo(workflow.name, null);
            break;
         case 'workflow-delete':
            this._deleteConfirmModal(this.deleteWorkflowModalTitle, () => this.onDeleteWorkflow.emit(workflow.name));
            break;
      }
   }

   selectVersionAction(event: string, version: any) {
      // this.generateVersion.emit();
      switch (event) {
         case 'version-new-workflow':
            this._duplicateWorkflow(version);
            break;
         case 'version-run-workflow':
            this.simpleRun(version);
            break;
         case 'version-run-params-workflow':
            this.showExecutionParams(version);
            break;
         case 'version-new-version':
            this.generateVersion.emit(version.id);
            break;
         case 'version-edit':
            this.route.navigate(['wizard/edit', version.id]);
            break;
         case 'version-delete':
            this._deleteConfirmModal(this.deleteVersionModalTitle, () => this.onDeleteVersion.emit(version.id));
            break;
      }
   }

   private showExecutionParams(version) {
      this.showExecutionConfig.emit(version);
   }

   private simpleRun(version) {
      this.onSimpleRun.emit({
         workflowId: version.id,
         workflowName: version.name
      });
   }

   private _duplicateWorkflow(version) {

      this._modalService.show({
         modalTitle: this.duplicateWorkflowTitle,
         maxWidth: 500,
         inputs: {
            version
         },
         outputs: {
            onCloseDuplicateModal: (response: any) => {
               this._modalService.close();
            }
         },
      }, DuplicateWorkflowComponent);
   }

   private _deleteConfirmModal(title: string, onSuccesHander: Function) {
      const buttons: StModalButton[] = [
         { label: 'Cancel', classes: 'button-secondary-gray', responseValue: StModalResponse.NO },
         { label: 'Delete', classes: 'button-critical', responseValue: StModalResponse.YES, closeOnClick: true }
      ];
      this._modalSubscription = this._modalService.show({
         messageTitle: this.deleteWorkflowModalMessage,
         modalTitle: title,
         buttons: buttons,
         maxWidth: 500,
         message: this.messageDeleteTitle,
      }).take(1).subscribe((response: any) => {
         if (response === 1) {
            this._modalService.close();
            this._modalSubscription.unsubscribe();
         } else if (response === 0) {
            onSuccesHander();
         }
      });
   }

   private _moveTo(workflow: any, groupName: string): void {
      this._modalService.show({
         modalTitle: this.moveGroupTitle,
         maxWidth: 500,
         inputs: {
            workflow: workflow,
            currentGroup: groupName,

         },
         outputs: {
            onCloseMoveGroup: (response: any) => {
               this._modalService.close();
            }
         },
      }, MoveGroupModal);
   }


   private _showNameEditionModal(title: string, entityType: string, entityName: string) {
      this._modalService.show({
         modalTitle: title,
         maxWidth: 500,
         inputs: {
            entityType,
            entityName
         },
         outputs: {
            onCloseRenameModal: (response: any) => {
               this._modalService.close();
            }
         },
      }, WorkflowRenameModal);
   }

   private scrollToTop() {
      if (window.pageYOffset > 0) {
         window.scrollTo(0, 0);
      }
   }

   constructor(private route: Router,
      private _cd: ChangeDetectorRef,
      private _translate: TranslateService,
      private _modalService: StModalService) {
      this.fields = [
         { id: 'name', label: 'Name' },
         { id: 'executionEngine', label: 'type' },
         { id: 'lastUpdateAux', label: 'Last update' },
         { id: 'options', label: '', sortable: false }
      ];

      this.versionFields = [
         { id: 'version', label: 'Version' },
         { id: 'tagsAux', label: 'Tags' },
         { id: 'lastUpdateAux', label: 'Last update' },
         { id: 'options', label: '', sortable: false }
      ];

      const deleteWorkflowModalTitle = 'DASHBOARD.DELETE_WORKFLOW_TITLE';
      const deleteFolderModalTitle = 'DASHBOARD.DELETE_FOLDER_TITLE';
      const deleteVersionModalTitle = 'DASHBOARD.DELETE_VERSION_TITLE';
      const deleteWorkflowModalMessage = 'DASHBOARD.DELETE_WORKFLOW_MESSAGE';
      const messageDeleteTitle = 'DASHBOARD.MESSAGE_DELETE_TITLE';
      const renameFolderTitle = 'DASHBOARD.RENAME_FOLDER_TITLE';
      const renameWorkflowTitle = 'DASHBOARD.RENAME_WORKFLOW_TITLE';
      const moveGroupTitle = 'DASHBOARD.MOVE_GROUP_TITLE';
      const deleteModalTitle = 'DASHBOARD.DELETE_TITLE';
      const duplicateWorkflowTitle = 'DASHBOARD.DUPLICATE_WORKFLOW';


      this._translate.get([deleteWorkflowModalTitle, deleteFolderModalTitle, deleteVersionModalTitle, deleteWorkflowModalMessage, messageDeleteTitle,
         renameFolderTitle, renameWorkflowTitle, moveGroupTitle, deleteModalTitle, duplicateWorkflowTitle]).subscribe(
         (value: { [key: string]: string }) => {
            this.deleteWorkflowModalTitle = value[deleteWorkflowModalTitle];
            this.deleteFolderModalTitle = value[deleteFolderModalTitle];
            this.deleteVersionModalTitle = value[deleteVersionModalTitle];
            this.deleteWorkflowModalMessage = value[deleteWorkflowModalMessage];
            this.messageDeleteTitle = value[messageDeleteTitle];
            this.renameFolderTitle = value[renameFolderTitle];
            this.renameWorkflowTitle = value[renameWorkflowTitle];
            this.moveGroupTitle = value[moveGroupTitle];
            this.deleteModalTitle = value[deleteModalTitle];
            this.duplicateWorkflowTitle = value[duplicateWorkflowTitle];
         });
   }
}
