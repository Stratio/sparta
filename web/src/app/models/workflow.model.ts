/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
interface WorkflowListTransformations {
    type: string;
    order: number;
    inputField: string;
    outputFields: {name: string, type: string}[];
    configuration: {whenError:string, removeInputField:boolean}
}

export interface WorkflowListType {
   id: string;
   context: any;
   executionEngine: string;
   group: string;
   outputs: any;
   input: any;
   cubes: any;
   streamTriggers: any;
   storageLevel: string;
   name: string;
   description: string;
   sparkStreamingWindow: string;
   transformations: any;
}

export interface WorkflowsType{
    workflowList: WorkflowListType;
    searchQuery: string;
}