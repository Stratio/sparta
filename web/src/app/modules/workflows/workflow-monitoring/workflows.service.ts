/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { formatDate, getFilterStatus } from '@utils';

@Injectable()
export class WorkflowsService {

    static normalizeWorkflow(workflow) {
        workflow.filterStatus = getFilterStatus(workflow.status.status);
        workflow.tagsAux = workflow.tags ? workflow.tags.join(', ') : '';
        try {
            const sparkURI = workflow.execution.marathonExecution.sparkURI;
            if (sparkURI.length) {
                workflow.sparkURI = sparkURI;
            }
        } catch (error) { }
        try {
            const lastErrorDate = workflow.execution.genericDataExecution.lastError.date;
            workflow.lastErrorDate = formatDate(lastErrorDate, true, true);
        } catch (error) { }
        try {
            const sparkURI = workflow.execution.localExecution.sparkURI;
            if (sparkURI.length) {
                workflow.sparkURI = sparkURI;
            }
        } catch (error) { }
        try {
            workflow.lastUpdate = workflow.status.lastUpdateDate ? formatDate(workflow.status.lastUpdateDate) : '';
            workflow.lastUpdateOrder = workflow.status.lastUpdateDate ? new Date(workflow.status.lastUpdateDate).getTime() : 0;
        } catch (error) { }

        return workflow;
    }
}
