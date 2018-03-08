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
    Output,
    ViewChild,
    ViewContainerRef,
    SimpleChanges
} from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { StModalService, StModalResponse, StModalButton } from '@stratio/egeo';

import { WorkflowsManagingService } from './../../workflows.service';
import { WorkflowRenameModal } from './../workflow-rename-modal/workflow-rename.component';
import { MoveGroupModal } from './../move-group-modal/move-group.component';
import { DuplicateWorkflowComponent } from './../duplicate-workflow-modal/duplicate-workflow.component';

@Component({
    selector: 'workflows-manage-header',
    styleUrls: ['workflows-header.component.scss'],
    templateUrl: 'workflows-header.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsManagingHeaderComponent implements OnChanges {

    @ViewChild('newWorkflowModal', { read: ViewContainerRef }) target: any;

    @Input() selectedWorkflows: Array<string>;
    @Input() selectedVersions: Array<string>;
    @Input() selectedVersionsData: Array<any>;
    @Input() selectedGroupsList: Array<string>;
    @Input() showDetails = false;
    @Input() levelOptions: Array<string>;
    @Input() versionsListMode = false;

    @Output() downloadWorkflows = new EventEmitter<void>();
    @Output() showWorkflowInfo = new EventEmitter<void>();
    @Output() onDeleteWorkflows = new EventEmitter<any>();
    @Output() onDeleteVersions = new EventEmitter<void>();
    @Output() changeFolder = new EventEmitter<number>();
    @Output() generateVersion = new EventEmitter<void>();
    @Output() onEditVersion = new EventEmitter<string>();

    public selectedVersionsInner: Array<string> = [];
    public selectedWorkflowsInner: Array<string> = [];
    public selectedGroupsListInner: Array<string> = [];

    public menuOptions: any = [];

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

        this.menuOptions = [
            {
                name: 'Group',
                value: 'group'
            }, {
                name: 'Workflow',
                subMenus: [
                    {
                        name: 'New workflow from scratch',
                        value: 'scratch',
                        subMenus: [
                            {
                                name: 'Streaming',
                                value: 'streaming'
                            },
                            {
                                name: 'Batch',
                                value: 'batch'
                            }
                        ]
                    },
                    {
                        name: 'New workflow from json file',
                        value: 'file'
                    }
                ]
            }
        ];
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

    public runWorkflow(version: any): void {
        if (this.isRunning(version)) {
            const stopPolicy = {
                'id': version.id,
                'status': 'Stopping'
            };
            this.workflowsService.stopWorkflow(stopPolicy);
        } else {
            this.workflowsService.runWorkflow(version.id, '');
        }
    }

    public isRunning(version: any) {
        const policyStatus = version.status.status;
        return policyStatus && policyStatus.toLowerCase() !== 'notstarted' && policyStatus.toLowerCase() !== 'failed' &&
            policyStatus.toLowerCase() !== 'stopped' && policyStatus.toLowerCase() !== 'stopping' &&
            policyStatus.toLowerCase() !== 'finished' && policyStatus.toLowerCase() !== 'created';
    }

    public editWorkflowGroup(): void {
        this._modalService.show({
            modalTitle: this.selectedGroupsListInner.length ? this.renameFolderTitle : this.renameWorkflowTitle,
            maxWidth: 500,
            inputs: {
                entityType: this.selectedGroupsListInner.length ? 'Group' : 'Workflow',
                entityName: this.selectedGroupsListInner.length ? this.selectedGroupsListInner[0] : this.selectedWorkflowsInner[0]
            },
            outputs: {
                onCloseRenameModal: (response: any) => {
                    this._modalService.close();
                }
            },
        }, WorkflowRenameModal);
    }

    public editVersion(): void {
        this.onEditVersion.emit(this.selectedVersions[0]);
    }

    public deleteWorkflows(): void {
        this.deleteWorkflowConfirmModal(this.selectedWorkflows);
    }

    public selectLevel(event: number): void {
        this.changeFolder.emit(event);
    }

    public moveTo(): void {
        this._modalService.show({
            modalTitle: this.moveGroupTitle,
            maxWidth: 500,
            inputs: {
                workflow: this.selectedWorkflowsInner.length ? this.selectedWorkflowsInner[0] : null,
                currentGroup: this.selectedGroupsListInner.length ? this.selectedGroupsListInner[0] : null,

            },
            outputs: {
                onCloseMoveGroup: (response: any) => {
                    this._modalService.close();
                }
            },
        }, MoveGroupModal);
    }

    public deleteWorkflowConfirmModal(workflows: Array<any>): void {
        const buttons: StModalButton[] = [
            { label: 'Cancel', classes: 'button-secondary-gray', responseValue: StModalResponse.NO },
            { label: 'Delete', classes: 'button-critical', responseValue: StModalResponse.YES, closeOnClick: true }
        ];
        this._modalService.show({
            messageTitle: this.deleteWorkflowModalMessage,
            modalTitle: this.deleteModalTitle,
            buttons: buttons,
            maxWidth: 500,
            message: this.messageDeleteTitle,
        }).subscribe((response: any) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                if (this.selectedVersions.length) {
                    this.onDeleteVersions.emit();
                } else {
                    this.onDeleteWorkflows.emit(workflows);
                }
            }
        });
    }

    public selectedMenuOption(event: any) {
        switch (event.value) {
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
}
