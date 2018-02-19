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
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { ApiService } from './api.service';
import { Http } from '@angular/http';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class WorkflowService extends ApiService {

    constructor(private _http: HttpClient, _store: Store<fromRoot.State>) {
        super(_http, _store);
    }

    getWorkflowList(): Observable<any> {

        const options: any = {};
        return this.request('workflows', 'get', options);
    }

    getWorkflowsByGroup(groupId: string): Observable<any> {
        const options: any = {};
        return this.request('workflows/findAllByGroup/' + groupId, 'get', options);
    }

    getGroups(): Observable<any> {
        const options: any = {};
        return this.request('groups', 'get', options);
    }

    createGroup(name: string): Observable<any> {
        const options: any = {
            body: {
                name: name
            }
        };
        return this.request('groups', 'post', options);
    }

    deleteGroupByName(name: string): Observable<any> {
        const options: any = {
        };
        return this.request('groups/deleteByName/' + name, 'delete', options);
    }

    deleteGroupById(groupId: string): Observable<any> {
        const options: any = {
        };
        return this.request('groups/deleteById/' + groupId, 'delete', options);
    }

    updateGroup(group: any): Observable<any> {
        const options: any = {
            body: group
        };
        return this.request('groups', 'put', options);
    }

    getWorkFlowContextList(): Observable<any> {

        const options: any = {};
        return this.request('workflowStatuses', 'get', options);
    }

    getWorkflowById(id: string): Observable<any> {
        const options: any = {};
        return this.request('workflows/findById/' + id, 'get', options);
    }

    saveWorkflow(json: any): Observable<any> {

        const options: any = {
            body: json
        };
        return this.request('workflows', 'post', options);
    }

    updateWorkflow(json: any): Observable<any> {

        const options: any = {
            body: json
        };
        return this.request('workflows', 'put', options);
    }

    downloadWorkflow(id: string): Observable<any> {

        const options: any = {};
        return this.request('workflows/download/' + id, 'get', options);
    }


    runWorkflow(id: string): Observable<any> {

        const options: any = {};
        return this.request('workflows/run/' + id, 'post', options);
    }

    stopWorkflow(status: any): Observable<any> {

        const options: any = {
            body: status
        };
        return this.request('workflowStatuses', 'put', options);
    }



    deleteWorkflow(id: string): Observable<any> {
        const options: any = {};
        return this.request('workflows/' + id, 'delete', options);
    }

    deleteWorkflowList(ids: Array<string>) {
        const options: any = {
            body: ids
        };
        return this.request('workflows/list', 'delete', options);
    }

    getWorkflowExecutionInfo(id: string) {
        const options: any = {};
        return this.request('workflowExecutions/' + id, 'get', options);
    }

    validateWorkflow(workflow: any) {
        const options: any = {
            body: workflow
        };
        return this.request('workflows/validate', 'post', options);
    }

    renameWorkflow(query: any) {
        const options: any = {
            body: query
        };
        return this.request('workflows/rename', 'put', options);
    }

    moveWorkflow(query: any) {
        const options: any = {
            body: query
        };
        return this.request('workflows/move', 'put', options);
    }

    generateVersion(workflow: any) {
        const options: any = {
            body: workflow
        };
        return this.request('workflows/version', 'post', options);
    }


}
