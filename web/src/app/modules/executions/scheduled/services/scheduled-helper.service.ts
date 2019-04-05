/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

 import { Injectable } from '@angular/core';
import { ScheduledExecution } from '../models/scheduled-executions';
import { formatDate } from '@utils';
 
 @Injectable()
 export class SchedulerHelperService {

    normalizeScheduledExecution(execution: any): ScheduledExecution {
        let initDateLocalized = '-';
        let initDateMillis = 0;
        let duration = '-';
        let durationMillis = 0;
        if(execution.duration) {
            duration = execution.duration;
            // const splitDuration = execution.duration.split(/(\d+)/);
            

        }
        try {
            initDateMillis = new Date(execution.initDate).getTime();
            initDateLocalized = formatDate(execution.initDate);
         } catch (error) { }
        return {
            id: execution.id,
            name: execution.entityName,
            initDateLocalized,
            initDateMillis,
            duration,
            executionEngine: execution.executionEngine,
            durationMillis,
            active: execution.active,
            executed: execution.state !== 'NOT_EXECUTED',
            groupName: execution.group.name,
            groupId: execution.group.id,
            version: execution.entityVersion,
            data: execution
        }
    }

 }