/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';

import { Observable } from 'rxjs/Observable';
import { ApiService} from './api.service';
import { HttpClient } from '@angular/common/http';

import * as fromRoot from 'reducers';

@Injectable()
export class BackupService extends ApiService {

    constructor(private _http: HttpClient, _store: Store<fromRoot.State>) {
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

    uploadBackup(file: any): Observable<any> {
        const fd = new FormData();
        fd.append('file', file);
        const options: any = {
            body: fd
        };
        return this.request('metadata/backup', 'put', options);
    }

    createBackupFile(data: any, fileName: string): void {
        const file = new Blob([JSON.stringify(data)], {type: 'json'});
        const a = document.createElement('a');
        a.href = URL.createObjectURL(file);
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
