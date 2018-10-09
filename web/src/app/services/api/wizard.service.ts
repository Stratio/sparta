/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Store } from '@ngrx/store';

import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import * as fromRoot from 'reducers';

@Injectable()
export class WizardApiService extends ApiService {

  constructor(private _http: HttpClient, _store: Store<fromRoot.State>) {
      super(_http, _store);
  }

  debug(workflow: any): Observable<any> {
    const options: any = {
      body: {
        workflowOriginal: workflow
      }
    };
    return this.request('debug', 'post', options);
  }

  debugWithExecutionContext(workflowId: string, executionContext: any): Observable<any> {
    const options = {
      body: {
        workflowId,
        executionContext
      }
    };
    return this.request('debug/runWithExecutionContext', 'post', options);
  }

  runDebug(workflowId: string): Observable<any> {
    const options: any = {};
    return this.request('debug/run/' + workflowId, 'post', options);
  }

  getDebugResult(workflowId: string): Observable<any> {
    const options: any = {};
    return this.request(`debug/resultsById/${workflowId}`, 'get', options);
  }

  uploadDebugFile(workflowId: string, file: any) {
    const fd = new FormData();
    fd.append('file', file);
    const options: any = {
      body: fd
    };
    return this.request('debug/uploadFile/' + workflowId, 'post', options);
  }

  deleteDebugFile(path: string) {
    const options: any = {};
    return this.request('debug/deleteFile/' + path, 'delete', options);
  }

  downloadDebugFile(path: string) {
    const options: any = {};
    return this.request('debug/downloadFile/' + path, 'get', options);
  }
}
