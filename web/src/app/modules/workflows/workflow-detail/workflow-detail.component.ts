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

import { Component, OnInit, Output, EventEmitter, ChangeDetectionStrategy, ChangeDetectorRef, Input } from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo';

@Component({
    selector: 'workflow-detail',
    templateUrl: './workflow-detail.template.html',
    styleUrls: ['./workflow-detail.styles.scss']
})
export class WorkflowDetailComponent implements OnInit {

    @Input() workflowData: any;
    @Output() showWorkflowExecutionInfo = new EventEmitter<any>();

    public inputs: Array<string> = [];
    public outputs: Array<string> = [];
    public transformations: Array<string> = [];

    ngOnChanges() {
       const inputs: Array<string> = [];
       const outputs: Array<string> = [];
       const transformations: Array<string> = [];

       if(this.workflowData) {
        this.workflowData.pipelineGraph.nodes.forEach((node: any) => {
            if(node.stepType.indexOf('Input') > -1){
                inputs.push(node.name);
            } else if(node.stepType.indexOf('Output') > -1) {
                outputs.push(node.name);
            } else {
                transformations.push(node.name);
            }
        });
       }

       this.inputs = inputs;
       this.outputs = outputs;
       this.transformations = transformations;
       this._cd.detectChanges();
    }

    constructor(private _cd: ChangeDetectorRef) { }

    ngOnInit() { }
}
