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

import { Injectable } from '@angular/core';

@Injectable()
export class InitializeWorkflowService {

    getInitializedWorkflow(workflow: any) {
        if (!workflow.uiSettings) {
            workflow.uiSettings = {};
            workflow.uiSettings.position = {
                x: 0,
                y: 0,
                k: 1
            };
        }
        const nodes = workflow.pipelineGraph.nodes;
        if (nodes && nodes.length && !nodes[0].uiConfiguration) {
            nodes.map((node: any) => {
                node.uiConfiguration = {};
                node.uiConfiguration.position = {
                    x: 0,
                    y: 0
                };
            });
        }
        return workflow;
    }

    constructor() {

    }
}


