/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    Output
} from '@angular/core';
import { StTableHeader, PaginateOptions, Order } from '@stratio/egeo';
import { Router } from '@angular/router';

import { MonitoringWorkflow } from './../../models/workflow';

@Component({
    selector: 'workflows-table',
    styleUrls: ['workflows-table.component.scss'],
    templateUrl: 'workflows-table.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsTableComponent {

    @Input() workflowList: Array<MonitoringWorkflow> = [];
    @Input() selectedWorkflowsIds: Array<string> = [];
    @Input() paginationOptions;
    @Input() currentOrder;

    @Output() onChangeOrder = new EventEmitter<Order>();
    @Output() selectWorkflow = new EventEmitter<MonitoringWorkflow>();
    @Output() deselectWorkflow = new EventEmitter<MonitoringWorkflow>();
    @Output() onChangePage = new EventEmitter<any>();
    @Output() changeCurrentPage = new EventEmitter<number>();

    public fields: StTableHeader[];
    public generatedId: string;

    public perPageOptions: PaginateOptions[] = [
        { value: 10, showFrom: 0 },
        { value: 20, showFrom: 0 },
        { value: 30, showFrom: 0 }
    ];

    checkValue(event: any) {
        this.checkRow(event.checked, event.value);
    }

    checkRow(isChecked: boolean, value: MonitoringWorkflow) {
        if (isChecked) {
            this.selectWorkflow.emit(value);
        } else {
            this.deselectWorkflow.emit(value);
        }
    }

    changePage(event) {
        this.onChangePage.emit();
        this.changeCurrentPage.emit(event);
    }

    showSparkUI(url: string) {
        window.open(url, '_blank');
    }

    editSelectedWorkflow($event: Event, workflowId: string) {
        $event.stopPropagation();
        this.route.navigate(['wizard', 'edit', workflowId]);
    }

    constructor(private route: Router, private _cd: ChangeDetectorRef) {
        this.generatedId = 'paginator-' + Math.floor((Math.random() * 1000) + 1);
        this.fields = [
            { id: 'isChecked', label: '', sortable: false },
            { id: 'name', label: 'Name' },
            { id: 'filterStatus', label: 'Status' },
            { id: 'startDate', label: 'Start Date' },
            { id: 'endDate', label: 'End Date' },
            { id: 'spark', label: '', sortable: false }
        ];
    }
}
