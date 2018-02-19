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