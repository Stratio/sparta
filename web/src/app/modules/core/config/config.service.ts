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

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';

@Injectable()
export class ConfigService {

   private _config:any;

   constructor(private http:Http) { }

   get config():any {
      return this._config;
   }

   set config(data:any) {
      this._config = data;
   }

   load(url:string):Promise<any> {
      return this.http
         .get(url)
         .map((res:Response) => res.json())
         .toPromise()
         .then((data:any) => this.config = data)
         .catch((err:any) => Promise.resolve());
   }
}
