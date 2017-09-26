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
import { Http, Response, RequestOptions, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';

export interface ApiRequestOptions {
   method: string;
   params?: Object;
   body?: Object;
}


@Injectable()
export class ApiService {

   private params: URLSearchParams = new URLSearchParams();
   private requestOptions: RequestOptions = new RequestOptions();

   constructor(
      private http: Http   ) { }

   request(url: string, options: ApiRequestOptions): Observable<any> {

      console.log(url);
      this.requestOptions.method = options.method;

      if (options.params) {
         this.requestOptions.search = this.generateParams(options.params);
      }

      if (options.body) {
         this.requestOptions.body = options.body;
      }

      return this.http.request(url, this.requestOptions)
         .map((res: Response) => {
               let response;
               try{
                  response = res.json();
               } catch (error) {
                  response = res.text();
               }
              return response;
          })
         .catch(this.handleError);

   }

   private generateParams(params: any): URLSearchParams {
      let object: URLSearchParams = new URLSearchParams();

      Object.keys(params).map(function (objectKey: any, index: any): void {
         let value: any = params[objectKey];
         object.set(objectKey, value);
      });

      return object;
   }

   private handleError(error: any): Observable<any> {
      return Observable.throw(error.json().message || 'Server error');
   }

}