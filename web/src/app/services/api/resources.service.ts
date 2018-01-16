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
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { ApiService} from './api.service';
import { Http } from '@angular/http';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ResourcesService extends ApiService {

    constructor(private _http: HttpClient, _store: Store<fromRoot.State>) {
        super(_http, _store);
    }

    getPluginsList(): Observable<any> {

        const options: any = {};
        return this.request('plugins', 'get', options);
    }

    getDriversList(): Observable<any> {

        const options: any = {};
        return this.request('driver', 'get', options);
    }

    uploadDriver(file: any): Observable<any> {
        let fd = new FormData();
        fd.append('file', file);
        const options: any = {
            body: fd
        };
        return this.request('driver', 'put', options);
    }


    uploadPlugin(file: any): Observable<any> {
        let fd = new FormData();
        fd.append('file', file);
        const options: any = {
            body: fd
        };
        return this.request('plugins', 'put', options);
    }

    deleteDriver(fileName: string): Observable<any> {
        const options: any = {};
        return this.request('driver/' + fileName, 'delete', options);
    }

    deletePlugin(fileName: string): Observable<any> {
        const options: any = {};
        return this.request('plugins/' + fileName, 'delete', options);
    }
}