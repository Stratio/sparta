/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { URLSearchParams } from '@angular/http';
import { Store } from '@ngrx/store';

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import { Observable, throwError } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { HttpErrorAction } from 'app/actions/errors';
import * as fromRoot from 'reducers';
import { catchError, map, take } from 'rxjs/operators';


export interface ApiRequestOptions {
  method: string;
  params?: Object;
  body?: Object;
}

let _expiredSession = false;

@Injectable()
export class ApiService {

   private requestOptions: any = {};
   private sessionGetParam = 'refresh-session';
   private timeout;
   constructor(private http: HttpClient, private _store: Store<fromRoot.State>) { 
      this.timeout = 20000;
      this._store.select(fromRoot.getSpartaTimeout)
      .pipe(take(1))
      .subscribe(timeout => this.timeout = timeout < 1000 ? timeout * 1000 : timeout);
   }

   request(url: string, method: string, options: any): Observable<any> {
      this.requestOptions = {};
      if (options.params) {
         this.requestOptions.search = this.generateParams(options.params);
      }

      if (options.body) {
         this.requestOptions.body = options.body;
      }

      this.requestOptions.responseType = 'text';
      this.requestOptions.headers = new HttpHeaders({ timeout: `${this.timeout}` });

      return this.http.request(method, url, this.requestOptions).pipe(map((res: any) => {
         try {
            if (res === 'OK') {
               return JSON.parse(JSON.stringify(res));
            }
            return JSON.parse(res);
         } catch (error) {
            if (res.indexOf('gosec-sso-ha') > -1) {
               this._showLoginWindow();
               throw new Error;
            }
            return res;
         }

      })).pipe(catchError(this.handleError.bind(this)));

   }

   private _showLoginWindow() {
      const refresh = this.getQueryParams(document.location.search)[this.sessionGetParam];
      this._store.dispatch(new HttpErrorAction(''));
      if (!refresh && !_expiredSession) {
         _expiredSession = true;
         const windowTop = (window.screen.height - 600) / 2;
         const windowLeft = (window.screen.width - 600) / 2;
         window.open('login?' + this.sessionGetParam + '=true', '_blank', `toolbar=yes,scrollbars=yes,resizable=yes, 
        top=${windowTop > 0 ? windowTop : 0},
        left=${windowLeft > 0 ? windowLeft : 0},
        width=600,height=600`);
      }
   }

   private generateParams(params: any): URLSearchParams {
      const object: URLSearchParams = new URLSearchParams();

      Object.keys(params).map(function (objectKey: any, index: any): void {
         const value: any = params[objectKey];
         object.set(objectKey, value);
      });

      return object;
   }

   private getQueryParams(qs: string) {
      qs = qs.split('+').join(' ');

      const params = {};
      let tokens;
      const re = /[?&]?([^=]+)=([^&]*)/g;

      while (tokens = re.exec(qs)) {
         params[decodeURIComponent(tokens[1])] = decodeURIComponent(tokens[2]);
      }

      return params;
   }

   private handleError(error: any): Observable<any> {
      if (error && error.status !== undefined && error.status === 0) {
         this._showLoginWindow();
      }
      return throwError(error);
   }
}
