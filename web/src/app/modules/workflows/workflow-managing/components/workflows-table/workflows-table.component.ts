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
import { StTableHeader } from '@stratio/egeo';
import { Router } from '@angular/router';

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

    @Output() onChangeOrder = new EventEmitter<any>();
    @Output() onChangeOrderVersions = new EventEmitter<any>();
    @Output() selectWorkflow = new EventEmitter<string>();
    @Output() selectGroup = new EventEmitter<string>();
    @Output() selectVersion = new EventEmitter<string>();
    @Output() openWorkflow = new EventEmitter<any>();
    @Output() changeFolder = new EventEmitter<any>();

    public fields: StTableHeader[];
    public versionFields: StTableHeader[];

    public openWorkflows: Array<string> = [];

    changeOrder($event: any): void {
        this.onChangeOrder.emit({
            orderBy: $event.orderBy,
            sortOrder: $event.type
        });
    }

    changeOrderVersions($event: any): void {
        this.onChangeOrderVersions.emit({
            orderBy: $event.orderBy,
            sortOrder: $event.type
        });
    }

    checkVersion(id: string) {
        this.selectVersion.emit(id);
    }

    checkWorkflow(workflow: any) {
        this.selectWorkflow.emit(workflow.name);
    }

    checkGroup(group: any) {
        this.selectGroup.emit(group.name);
    }

    openWorkflowClick(event: any, workflow: any) {
        event.stopPropagation();
        this.openWorkflow.emit(workflow);
    }

    openGroup(event: any, group: any) {
        event.stopPropagation();
       this.changeFolder.emit(group);
    }

    showSparkUI(url: string) {
        window.open(url, '_blank');
    }

    editVersion(event: any, versionId: string) {
        event.stopPropagation();
        this.route.navigate(['wizard/edit', versionId]);
    }

    constructor(private route: Router, private _cd: ChangeDetectorRef) {

        this.fields = [
            { id: 'isChecked', label: '', sortable: false },
            { id: 'name', label: 'Name' },
            { id: 'executionEngine', label: 'type' },
            { id: 'lastUpdateAux', label: 'Last update' }
        ];

        this.versionFields = [
            { id: 'isChecked', label: '', sortable: false },
            { id: 'version', label: 'Version' },
            { id: 'status.status', label: 'Advanced Status' },
            { id: 'lastUpdateAux', label: 'Last update' }
        ];
    }
}
