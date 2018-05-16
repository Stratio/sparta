/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export class MonitoringWorkflow {
    id: string;
    name: string;
    description: string;
    execution: MonitoringExecution;
    settings?: {
        executionMode: string;
        userPluginsJars: Array<string>;
        initSqlSentences: Array<string>;
        addAllUploadedPlugins: boolean;
        mesosConstraint: string;
        mesosConstraintOperator: string
    };
    nodes: Array<MonitoringWorkflowNode>;
    executionEngine: 'Batch' | 'Streaming';
    lastUpdate?: string;
    version: number;
    group: string;
    status?: MonitoringWorkflowStatus;
}

export class MonitoringWorkflowNode {
    name: string;
    stepType: string;
}

export class MonitoringExecution {
    genericDataExecution: {
        lastError: {
            message: string;
            phase: string;
            originalMsg: string;
            date: string;
        };
        lastExecutionMode: string;
    };
}

export class MonitoringWorkflowStatus {
    id: string;
    status: string;
    statusInfo: string;
    lastUpdateDate: string;
}
