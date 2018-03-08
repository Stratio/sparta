/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
        let x = 40; // default node positions
        let y = 100;
        let h = true;
        if (nodes && nodes.length && !nodes[0].uiConfiguration) {
            nodes.map((node: any) => {
                node.uiConfiguration = {};
                node.uiConfiguration.position = {
                    x: x,
                    y: y
                };

                h ? x += 200 : y += 120;
                h = !h;
            });
        }
        return workflow;
    }

    constructor() {

    }
}


