/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { URLSearchParams } from '@angular/http';

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import { Observable } from 'rxjs/Observable';
import { HttpClient, HttpHeaders } from '@angular/common/http';

export interface ApiRequestOptions {
   method: string;
   params?: Object;
   body?: Object;
}


@Injectable()
export class ApiService {

   private requestOptions: any = {};

   constructor(private http: HttpClient) { }

   request(url: string, method: string, options: any): Observable<any> {
      this.requestOptions = {};
      if (options.params) {
         this.requestOptions.search = this.generateParams(options.params);
      }

      if (options.body) {
         this.requestOptions.body = options.body;
      }

      this.requestOptions.responseType = 'text';
      this.requestOptions.headers = new HttpHeaders({ timeout: `${20000}` });

      return this.http.request(method, url, this.requestOptions).map((res: any) => {
         try {
            return JSON.parse(res);
         } catch (error) {
            if (res.indexOf('gosec-sso-ha') > -1) {
               window.location.href = 'login';
               throw new Error;
            }
            return res;
         }

      }).catch(this.handleError);

   }

   private generateParams(params: any): URLSearchParams {
      const object: URLSearchParams = new URLSearchParams();

      Object.keys(params).map(function (objectKey: any, index: any): void {
         const value: any = params[objectKey];
         object.set(objectKey, value);
      });

      return object;
   }

   private handleError(error: any): Observable<any> {
      return Observable.throw(error);
   }
}
