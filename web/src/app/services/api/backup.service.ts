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
import { HttpClient } from '@angular/common/http';

@Injectable()
export class BackupService extends ApiService {

    constructor(private _http: HttpClient, private configService: ConfigService, _store: Store<fromRoot.State>) {
        super(_http, _store);
    }

    getBackupList(): Observable<any> {

        const options: any = {};
        return this.request('metadata/backup', 'get', options);
    }

    generateBackup(): Observable<any> {

        const options: any = {};
        return this.request('metadata/backup/build', 'get', options);
    }

    deleteBackup(fileName: string): Observable<any> {

        const options: any = {};

        return this.request('metadata/backup/' + fileName, 'delete', options);
    }

    downloadBackup(fileName: string): Observable<any> {

        const options: any = {};
        return this.request('metadata/backup/' + fileName, 'get', options);
    }

    executeBackup(fileName: string, removeData: boolean): Observable<any> {

        const options: any = {
            body: {
                fileName: fileName,
                deleteAllBefore: removeData
            }
        };
        return this.request('metadata/backup', 'post', options);
    }

    uploadBackup(file:any): Observable<any> {
        const fd = new FormData();
        fd.append('file', file);
        const options: any = {
            body: fd
        };
        return this.request('metadata/backup', 'put', options);
    }

    createBackupFile(data: any, fileName: string): void {
        const backupData = 'text/json;charset=utf-8,' + JSON.stringify(data);
        const a = document.createElement('a');
        a.href = 'data:' + backupData;
        a.download = fileName + '.json';
        document.body.appendChild(a);
        a.click();
        a.remove();
    }

    deleteAllBackups(): Observable<any> {

        const options: any = {};
        return this.request('metadata/backup', 'delete', options);
    }


    deleteMetadata(): Observable<any> {

        const options: any = {};
        return this.request('metadata', 'delete', options);
    }
}
