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
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnInit,
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

export class WorkflowsTableComponent implements OnInit {

    @Input() workflowList: Array<any> = [];
    @Input() selectedWorkflowsIds: Array<string> = [];
    @Input() paginationOptions;
    @Input() currentOrder;

    @Output() onChangeOrder = new EventEmitter<any>();
    @Output() selectWorkflow = new EventEmitter<any>();
    @Output() deselectWorkflow = new EventEmitter<any>();
    @Output() onChangePage = new EventEmitter<any>();
    @Output() changeCurrentPage = new EventEmitter<number>();

    public fields: StTableHeader[];
    public generatedId: string;

    public perPageOptions: any = [
        { value: 10, showFrom: 0 }, { value: 20, showFrom: 0 }, { value: 30, showFrom: 0 }
    ];

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

    changePage($event: any) {
        this.onChangePage.emit();
        this.changeCurrentPage.emit($event);
    }

    showSparkUI(url: string) {
        window.open(url, '_blank');
    }

    editSelectedWorkflow($event: any, workflowId: string) {
        $event.stopPropagation();
        this.route.navigate(['wizard', 'edit', workflowId]);
    }

    ngOnInit(): void {

    }

    constructor(private route: Router, private _cd: ChangeDetectorRef) {
        this.generatedId = 'paginator-' + Math.floor((Math.random() * 1000) + 1);
        this.fields = [
            { id: 'isChecked', label: '', sortable: false },
            { id: 'name', label: 'Name' },
            { id: 'filterStatus', label: 'Status' },
            { id: 'tagsAux', label: 'Tags' },
            { id: 'lastUpdateOrder', label: 'Last status update' },
            { id: 'spark', label: '', sortable: false }
        ];
    }

}
