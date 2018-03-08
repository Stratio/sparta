/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

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