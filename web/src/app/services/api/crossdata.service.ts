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
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';

@Injectable()
export class CrossdataService extends ApiService {

    constructor(private _http: Http, private configService: ConfigService, _store: Store<fromRoot.State>) {
        super(_http, _store);
    }

    getCrossdataDatabases(): Observable<any> {
        const options: ApiRequestOptions = {
            method: 'get'
        };
        return this.request('crossdata/databases', options);
    }

    getCrossdataTables(): Observable<any> {

        const options: ApiRequestOptions = {
            method: 'get'
        };
        return this.request('crossdata/tables', options);
    }


    getDatabaseTables(query: any): Observable<any> {

        const options: ApiRequestOptions = {
            method: 'post',
            body: query
        };
        return this.request('crossdata/tables', options);
    }

    getCrossdataTableInfo(tableName: string): Observable<any> {

        const options: ApiRequestOptions = {
            method: 'post',
            body: {
                tableName: tableName
            }
        };
        return this.request('crossdata/tables/info', options);
    }

    executeCrossdataQuery(query: string): Observable<any> {

        const options: ApiRequestOptions = {
            method: 'post',
            body: {
                query: query
            }
        };
        return this.request('crossdata/queries', options);
    }

}
