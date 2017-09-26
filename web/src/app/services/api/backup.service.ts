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


@Injectable()
export class BackupService extends ApiService {

    constructor(private _http: Http, private configService: ConfigService) {
        super(_http);
    }

    getBackupList(): Observable<any> {

        let options: ApiRequestOptions = {
            method: 'get'
        };
        return this.request(this.configService.config.API_URL + '/metadata/backup', options);
    }

    generateBackup(): Observable<any> {

        let options: ApiRequestOptions = {
            method: 'get'
        };
        return this.request(this.configService.config.API_URL + '/metadata/backup/build', options);
    }

    deleteBackup(fileName: string): Observable<any> {

        let options: ApiRequestOptions = {
            method: 'delete'
        };
        return this.request(this.configService.config.API_URL + '/metadata/backup/' + fileName, options);
    }

    downloadBackup(fileName: string): Observable<any> {

        let options: ApiRequestOptions = {
            method: 'get'
        };
        return this.request(this.configService.config.API_URL + '/metadata/backup/' + fileName, options);
    }

    executeBackup(fileName: string, removeData: boolean): Observable<any> {

        let options: ApiRequestOptions = {
            method: 'post',
            body: {
                fileName: fileName,
                deleteAllBefore: removeData
            }
        };
        return this.request(this.configService.config.API_URL + '/metadata/backup', options);
    }

    uploadBackup(file:any): Observable<any> {
        let fd = new FormData();
        fd.append('file', file);
        let options: ApiRequestOptions = {
            method: 'put',
            body: fd
        };
        return this.request(this.configService.config.API_URL + '/metadata/backup', options);
    }

    createBackupFile(data: any, fileName: string): void {
        console.log(data);
        const backupData = 'text/json;charset=utf-8,' + JSON.stringify(data);
        const a = document.createElement('a');
        a.href = 'data:' + backupData;
        a.download = fileName + '.json';
        document.body.appendChild(a);
        a.click();
        a.remove();
    }

    deleteAllBackups(): Observable<any> {

        let options: ApiRequestOptions = {
            method: 'delete'
        };
        return this.request(this.configService.config.API_URL + '/metadata/backup', options);
    }


    deleteMetadata(): Observable<any> {

        let options: ApiRequestOptions = {
            method: 'delete'
        };
        return this.request(this.configService.config.API_URL + '/metadata', options);
    }
}
