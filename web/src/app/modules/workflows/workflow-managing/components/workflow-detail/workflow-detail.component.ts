/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, Output, EventEmitter, ChangeDetectorRef, Input } from '@angular/core';

import { DataDetails } from './../../models/data-details';
import { FOLDER_SEPARATOR } from '../../workflow.constants';

@Component({
    selector: 'workflow-managing-detail',
    templateUrl: './workflow-detail.template.html',
    styleUrls: ['./workflow-detail.styles.scss']
})
export class WorkflowManagingDetailComponent implements OnInit {

    @Input() data: DataDetails;
    @Output() showWorkflowExecutionInfo = new EventEmitter<any>();

    public inputs: Array<string> = [];
    public outputs: Array<string> = [];
    public transformations: Array<string> = [];

    public workflowData: any;
    public groupLabel = '';

    ngOnChanges() {
        if (this.data) {
            this.workflowData = this.data.data;
            if (this.data.type === 'version') {
                this.getWorkflowData();
            } else if(this.data.type === 'group') {
                const split = this.data.data.name.split(FOLDER_SEPARATOR);
                this.groupLabel = split[split.length - 1];
            }

            this._cd.detectChanges();
        }
    }

    getWorkflowData() {
        const inputs: Array<string> = [];
        const outputs: Array<string> = [];
        const transformations: Array<string> = [];

        if (this.data) {
            this.data.data.nodes.forEach((node: any) => {
                if (node.stepType.indexOf('Input') > -1) {
                    inputs.push(node.name);
                } else if (node.stepType.indexOf('Output') > -1) {
                    outputs.push(node.name);
                } else {
                    transformations.push(node.name);
                }
            });
        }

        this.inputs = inputs;
        this.outputs = outputs;
        this.transformations = transformations;
    }

    constructor(private _cd: ChangeDetectorRef) { }

    ngOnInit() { }
}
