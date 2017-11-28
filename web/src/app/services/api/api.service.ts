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
import { URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Store } from '@ngrx/store';
import { HttpClient } from '@angular/common/http';
import * as fromRoot from 'reducers';


export interface ApiRequestOptions {
      method: string;
      params?: Object;
      body?: Object;
}


@Injectable()
export class ApiService {

      private params: URLSearchParams = new URLSearchParams();
      private requestOptions: any = {};

      constructor(private http: HttpClient, private store: Store<fromRoot.State>) { }

      request(url: string, method: string, options: any): Observable<any> {

            if (options.params) {
                  this.requestOptions.search = this.generateParams(options.params);
            }

            if (options.body) {
                  this.requestOptions.body = options.body;
            }

            this.requestOptions.responseType = 'text';

            return this.http.request(method, url, this.requestOptions).map((res: any) => {
                  try {
                        return JSON.parse(res);
                  } catch (error) {
                        return res;
                  }

            }).catch(this.handleError);

      }

      private generateParams(params: any): URLSearchParams {
            const object: URLSearchParams = new URLSearchParams();

            Object.keys(params).map(function (objectKey: any, index: any): void {
                  let value: any = params[objectKey];
                  object.set(objectKey, value);
            });

            return object;
      }

      private handleError(error: any): Observable<any> {
            return Observable.throw(error);
      }
}
