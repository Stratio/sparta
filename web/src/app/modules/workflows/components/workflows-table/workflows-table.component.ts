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
    Output
} from '@angular/core';
import { StTableHeader } from '@stratio/egeo';
import { Router } from '@angular/router';

@Component({
    selector: 'workflows-table',
    styleUrls: ['workflows-table.component.scss'],
    templateUrl: 'workflows-table.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsTableComponent {

    @Input() workflowList: Array<any> = [];
    @Input() selectedWorkflowsIds: Array<string> = [];

    @Output() onChangeOrder = new EventEmitter<any>();
    @Output() selectWorkflow = new EventEmitter<any>();
    @Output() deselectWorkflow = new EventEmitter<any>();

    public fields: StTableHeader[];

    changeOrder($event: any): void {
        this.onChangeOrder.emit({
            orderBy: $event.orderBy,
            sortOrder: $event.type
        });
    }

    checkValue(event: any) {
        this.checkRow(event.checked, event.value);
    }

    checkRow(isChecked: boolean, value: any) {
        if (isChecked) {
            this.selectWorkflow.emit(value);
        } else {
            this.deselectWorkflow.emit(value);
        }
    }

    showSparkUI(url: string) {
        window.open(url, '_blank');
    }

    editSelectedWorkflow($event: any, workflowId: string) {
        $event.stopPropagation();
        this.route.navigate(['wizard', 'edit', workflowId]);
    }

    constructor(private route: Router) {
        this.fields = [
            { id: 'isChecked', label: '', sortable: false },
            { id: 'name', label: 'Name' },
            { id: 'executionEngine', label: 'type' },
            { id: 'context.status', label: 'Status' },
            { id: 'spark', label: '', sortable: false }
        ];
    }
}
