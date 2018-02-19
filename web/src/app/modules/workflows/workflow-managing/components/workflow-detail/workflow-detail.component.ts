import { FOLDER_SEPARATOR } from '../../workflow.constants';
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

import { Component, OnInit, Output, EventEmitter, ChangeDetectorRef, Input } from '@angular/core';
import { DataDetails } from './../../models/data-details';

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
