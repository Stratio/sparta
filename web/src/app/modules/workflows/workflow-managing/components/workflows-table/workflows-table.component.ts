///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

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
            { id: 'status.status', label: 'Status' },
            { id: 'lastUpdateAux', label: 'Last update' }
        ];
    }
}
