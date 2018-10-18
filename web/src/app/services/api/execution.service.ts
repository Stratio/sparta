
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';

import { ApiService } from './api.service';
import * as fromRoot from 'reducers';

@Injectable()
export class ExecutionService extends ApiService {

   constructor(private _http: HttpClient, _store: Store<fromRoot.State>) {
      super(_http, _store);
   }

   getExecutions(): Observable<any> {
      const options: any = {};
      return this.request('workflowExecutions', 'get', options);
   }

   getDashboardExecutions(): Observable<any> {
      const options: any = {};
      return this.request('workflowExecutions/dashboard', 'get', options);
   }

   getAllExecutions(): Observable<any> {
      const options: any = {};
      return this.request('workflowExecutions/findAllDto', 'get', options);
   }

   getExecutionsByQuery(query: any): Observable<any> {
      const options = {
         body: query
      };
      return this.request('workflowExecutions/findByQuery', 'post', options);
   }

   archiveExecution(executionId: string, archived: boolean): Observable<any> {
      const options = {
         body: {
            executionId,
            archived
         }
      };
      return this.request('workflowExecutions/archived', 'post', options);
   }

   stopExecutionsById(id: string): Observable<any> {
      const options: any = {};
      return this.request(`workflowExecutions/stop/${id}`, 'post', options);
   }


   getWorkflowExecutionInfo(id: string) {
      const options: any = {};
      return this.request('workflowExecutions/' + id, 'get', options);
   }

   deleteExecution(id: string) {
     const options: any = {};
     return this.request('workflowExecutions/' + id, 'delete', options);
   }


}



