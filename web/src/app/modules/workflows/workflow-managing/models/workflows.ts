/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export class GroupWorkflow {
    id: string;
    name: string;
    description: string;
    group: string;
    settings: {

    };
    nodes: GroupWorkflowNode[];
    executionEngine: 'Batch' | 'Streaming';
    lastUpdateDate: string;
    version: number;
    status: {
        status: string;
        statusInfo: string;
        lastExecutionMode: string;
        lastError: {
            message: string;
            phase: string;
            originalMsg: string;

            date: string
        };
        lastUpdateDate: string;
    };
}

export class GroupWorkflowNode {
    name: string;
    stepType: 'Input' | 'Output' | 'Transformation'
}

export class Group {
    id: string;
    name: string;
    label: string;
}
