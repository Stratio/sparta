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
import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
    HttpResponse
} from '@angular/common/http';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';

import * as errorsActions from 'actions/errors';
import * as fromRoot from 'reducers';


@Injectable()
export class ApiInterceptor implements HttpInterceptor {
    constructor(private store: Store<fromRoot.State>) { }
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

        return next.handle(request).do((event: HttpEvent<any>) => {
            if (event instanceof HttpResponse) {
                // intercept request. TODO jwt token?
            }
        }, (err: any) => {
            if (err instanceof HttpErrorResponse) {

                console.log(err);
                if (err.status === 401) {
                    this.store.dispatch(new errorsActions.ForbiddenErrorAction(''));
                }

                if (err.error) {
                    try {
                        const error = JSON.parse(err.error);
                        if (error.message && error.exception) {
                            this.store.dispatch(new errorsActions.ServerErrorAction({
                                title: error.message,
                                description: error.exception
                            }));
                        }
                    } catch (e) {

                    }
                }
            }
        });
    }
}