/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Store } from '@ngrx/store';

import * as fromRoot from 'reducers';
import { ApiService } from './api.service';

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
    return this.request('workflows/findAllByGroupDto/' + groupId, 'get', options);
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

  findAllMonitoring(): Observable<any> {
    const options: any = {};
    return this.request('workflows/findAllMonitoring', 'get', options);
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

  validateWithExecutionContext(data: any): Observable<any> {
    const options: any = {
      body: data
    };
    return this.request('workflows/validateWithExecutionContext', 'post', options);
  }

  runWorkflow(id: string): Observable<any> {

    const options: any = {};
    return this.request('workflows/run/' + id, 'post', options);
  }


  runWorkflowWithParams(data: any): Observable<any> {

    const options: any = {
      body: data
    };
    return this.request('workflows/runWithExecutionContext', 'post', options);
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

  getRunParameters(workflowId: string) {
    const options: any = {};
    return this.request('workflows/runWithParametersViewById/' + workflowId, 'post', options);
  }

  getRunParametersFromWorkflow(workflow: any) {
    const options: any = {
      body: workflow
    };
    return this.request('workflows/runWithParametersView', 'post', options);
  }
}
