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
export class TemplatesService extends ApiService {

    constructor(private _http: HttpClient) {
        super(_http);
    }

    getAllTemplates(): Observable<any> {
        const options: any = {};
        return this.request('template', 'get', options);
    }

    getTemplateList(templateType: string): Observable<any> {
        const options: any = {};
        return this.request('template/' + templateType, 'get', options);
    }

    getTemplateById(templateType: string, templateId: string): Observable<any> {
        const options: any = {};
        return this.request('template/' + templateType + '/id/' + templateId, 'get', options);
    }

    deleteTemplate(templateType: string, templateId: string): Observable<any> {
        const options: any = {};
        return this.request('template/' + templateType + '/id/' + templateId, 'delete', options);
    }

    validateTemplateName(templateType: string, templateName: string): Observable<any> {
        const options: any = {};
        return this.request('template/' + templateType + '/name/' + templateName, 'get', options);
    }

    createTemplate(templateData: any): Observable<any> {
        const options: any = {
            body: templateData
        };
        return this.request('template', 'post', options);
    }

    updateFragment(fragmentData: any): Observable<any> {
        const options: any = {
            body: fragmentData
        };
        return this.request('template', 'put', options);
    }
}
