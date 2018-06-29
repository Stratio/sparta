/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { formatDate, getFilterStatus } from '@utils';

@Injectable()
export class WorkflowsService {

    normalizeWorkflow(workflow) {
        workflow.filterStatus = getFilterStatus(workflow.status.status);
        workflow.tagsAux = workflow.tags ? workflow.tags.join(', ') : '';
        try {
            const sparkURI = workflow.execution.marathonExecution.sparkURI;
            if (sparkURI.length) {
                workflow.sparkURI = sparkURI;
            }
        } catch (error) { }
        try {
            const startDate = workflow.execution.genericDataExecution.startDate;
            workflow.startDate = formatDate(startDate);
        } catch (error) { }
        try {
            const endDate = workflow.execution.genericDataExecution.endDate;
            workflow.endDate = formatDate(endDate);
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

        try {
            const launchDate = workflow.execution.genericDataExecution.launchDate;
            workflow.launchDate = formatDate(launchDate);
        } catch (error) { }

        if (workflow.startDate && workflow.endDate) {
            try {
                const startDate = new Date(workflow.execution.genericDataExecution.startDate).getTime();
                const endDate = new Date(workflow.execution.genericDataExecution.endDate).getTime();
                workflow.executionTime = this._msToTime(endDate - startDate);
            } catch (error) { }
        }
        return workflow;
    }

    private _msToTime(duration: number) {
        let seconds = duration / 1000;
        const hours = parseInt((seconds / 3600).toString()); // 3,600 seconds in 1 hour
        seconds = seconds % 3600; // seconds remaining after extracting hours
        const minutes = parseInt((seconds / 60).toString()); // 60 seconds in 1 minute
        seconds = seconds % 60;
        let res = '';
        if (hours > 0) {
            res += hours + ' hour';
            res += hours === 1 ? '' : 's';
            res +=  ', ';
        }
        if (minutes > 0) {
            res += minutes + ' minute';
            res += minutes === 1 ? '' : 's';
            res +=  ', ';
        }
         res += seconds + ' second';
         res += seconds === 1 ? '' : 's';
        return res;
    }
}
