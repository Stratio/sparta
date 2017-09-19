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

import { ConfigService } from '@app/core';

import { Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { ApiService, ApiRequestOptions } from './api.service';
import { Http } from '@angular/http';


@Injectable()
export class WorkflowService extends ApiService {

   constructor(private _http: Http, private configService: ConfigService) {
      super(_http);
   }

   getWorkflowList(): Observable<any> {

      const options: ApiRequestOptions = {
         method: 'get'
      };
      return this.request(this.configService.config.API_URL + '/workflows/all', options);
   }

   getWorkFlowContextList(): Observable<any> {

      const options: ApiRequestOptions = {
         method: 'get'
      };
      return this.request(this.configService.config.API_URL + '/workflowStatuses', options);
   }

   getWorkflowByName(name: string): Observable<any> {

      const options: ApiRequestOptions = {
         method: 'get'
      };
      return this.request(this.configService.config.API_URL + '/workflows/findByName/' + name, options);
   }

   saveWorkflow(json: any): Observable<any> {

      const options: ApiRequestOptions = {
         method: 'post',
         body: json
      };
      return this.request(this.configService.config.API_URL + '/workflows', options);
   }

   downloadWorkflow(id: string): Observable<any> {

      const options: ApiRequestOptions = {
         method: 'get'
      };
      return this.request(this.configService.config.API_URL + '/workflows/download/' + id, options);
   }


   runWorkflow(id: string): Observable<any> {

      const options: ApiRequestOptions = {
         method: 'get'
      };
      return this.request(this.configService.config.API_URL + '/workflows/run/' + id, options);
   }

   stopWorkflow(status: any): Observable<any> {

      const options: ApiRequestOptions = {
         method: 'put',
         body: status

      };
      return this.request(this.configService.config.API_URL + '/workflowStatuses', options);
   }



   deleteWorkflow(id: string): Observable<any> {

      const options: ApiRequestOptions = {
         method: 'delete'
      };
      return this.request(this.configService.config.API_URL + '/workflows/' + id, options);
   }
}
