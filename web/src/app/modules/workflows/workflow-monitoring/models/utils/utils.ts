/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { trim as _trim } from 'lodash';

export class WorkflowUtils {

    static searchWorkflows(workflowList: Array<any>, searchQuery: string) {
        return searchQuery.length ? workflowList.filter((workflow: any) => {
            const status = workflow.status.status === 'Started' ? 'Running' : workflow.status.status;
            const query = searchQuery.toLowerCase();
            const queryNoSpaces = _trim(query);
            switch (true) {
                case (`v${workflow.version} - ${workflow.name}`).toLowerCase().indexOf(query) > -1:
                    return true;
                case workflow.tagsAux && workflow.tagsAux.toLowerCase().indexOf(query) > -1:
                    return true;
                case workflow.group && workflow.group.indexOf(query) > -1:
                    return true;
                case workflow.executionEngine.toLowerCase().indexOf(query) > -1:
                    return true;
                case status.toLowerCase().indexOf(queryNoSpaces) > -1:
                    return true;
                case workflow.filterStatus && workflow.filterStatus.toLowerCase().indexOf(queryNoSpaces) > -1:
                    return true;
                default:
                    return false;
            }
        }) : workflowList;
    }
};
