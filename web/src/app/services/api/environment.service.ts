/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class EnvironmentService extends ApiService {

    constructor(private _http: HttpClient) {
        super(_http);
    }

    getEnvironment(): Observable<any> {
        const options: any = {};
        return this.request('environment', 'get', options);
    }

    saveEnvironment(environment: any): Observable<any> {
        const options: any = {
            body: environment
        };
        return this.request('environment', 'post', options);
    }

    updateEnvironment(environment: any): Observable<any> {
        const options: any = {
            body: environment
        };
        return this.request('environment', 'put', options);
    }

    exportEnvironment(): Observable<any> {
        const options: any = {
        };
        return this.request('environment/export', 'get', options);
    }


    importEnvironment(environmentData: any): Observable<any> {
        const options: any = {
            body: environmentData
        };
        return this.request('environment/import', 'put', options);
    }

}
