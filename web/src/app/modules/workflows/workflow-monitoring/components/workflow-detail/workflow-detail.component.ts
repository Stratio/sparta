/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Output, EventEmitter, ChangeDetectorRef, Input, OnChanges } from '@angular/core';

import { MonitoringExecution, MonitoringWorkflow } from './../../models/workflow';
@Component({
    selector: 'workflow-detail',
    templateUrl: './workflow-detail.template.html',
    styleUrls: ['./workflow-detail.styles.scss']
})
export class WorkflowDetailComponent implements OnInit, OnChanges {

    @Input() workflowData: MonitoringWorkflow;
    @Output() showWorkflowExecutionInfo = new EventEmitter<any>();
    @Output() showConsole = new EventEmitter<any>();


    public inputs: Array<string> = [];
    public outputs: Array<string> = [];
    public transformations: Array<string> = [];

    public lastError: any;
    public execution: MonitoringExecution;

    ngOnChanges() {
        this.execution = this.workflowData && this.workflowData.execution ? this.workflowData.execution : null;
        this.lastError = this.execution && this.execution.genericDataExecution ?
            this.workflowData.execution.genericDataExecution.lastError : null;
        this._cd.detectChanges();
    }

    constructor(private _cd: ChangeDetectorRef) { }

    ngOnInit() { }

    onShowConsole() {
       this.showConsole.emit();
    }
}
