/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';

import { ApiService} from './api.service';
import * as fromRoot from 'reducers';

@Injectable()
export class ParametersService extends ApiService {

   constructor(private _http: HttpClient, _store: Store<fromRoot.State>) {
      super(_http, _store);
   }

   getGlobalParameters(): Observable<any> {
      const options: any = {};
      return this.request('globalParameters', 'get', options);
   }

   saveGlobalParameter(query: any): Observable<any> {
      const options: any = {
         body: query
      };
      return this.request('globalParameters', 'post', options);
   }

   updateGlobalParameter(query: any): Observable<any> {
      const options: any = {
         body: query
      };
      return this.request('globalParameters', 'put', options);
   }

   getEnvironmentParameters(): Observable<any> {
      const options: any = {};
      return this.request('paramList/environment', 'get', options);
   }

   getEnvironmentAndContext(): Observable<any> {
      const options: any = {};
      return this.request('paramList/environmentAndContexts', 'get', options);
   }
   getEnvironmentContexts(): Observable<any> {
      const options: any = {};
      return this.request('paramList/environmentContexts', 'get', options);
   }

   getCustomAndContext(name: string): Observable<any> {
      const options: any = {};
      return this.request(`paramList/parentAndContexts/${name}`, 'get', options);
   }

   deleteList(id: string): Observable<any> {
      const options: any = {};
      return this.request(`paramList/id/${id}`, 'delete', options);
   }

   getCustomContexts(name: string): Observable<any> {
      const options: any = {};
      return this.request(`paramList/contexts/${name}`, 'get', options);
   }

   getParamList(): Observable<any> {
      const options: any = {};
      return this.request('paramList', 'get', options);
   }

   createParamList(query: any): Observable<any> {
      const options: any = {
         body: query
      };
      return this.request('paramList', 'post', options);
   }

   updateParamList(query: any): Observable<any> {
      const options: any = {
         body: query
      };
      return this.request('paramList', 'put', options);
   }

}


