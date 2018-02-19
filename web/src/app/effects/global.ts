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

import { GlobalConfigService } from 'services/templates.service';
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';

import { Effect, Actions } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import * as userActions from 'actions/user';
import * as errorsActions from 'actions/errors';

@Injectable()
export class GlobalEffect {

    @Effect()
    getUserProfile$: Observable<Action> = this.actions$
        .ofType(userActions.GET_USER_PROFILE).switchMap((response: any) => {
            return this.configService.getConfig()
                .map((config: any) => {
                    return new userActions.GetUserProfileCompleteAction(config);
                }).catch(function (error: any) {
                    return Observable.of(new userActions.GetUserProfileErrorAction(''));
                });
        });


    @Effect()
    showAlertNotifications: Observable<Action> = this.actions$
        .ofType(errorsActions.SERVER_ERROR).map((errorResponse: any) => {
            const err = errorResponse.payload;
            if (err.status !== 401 && err.error) {
                try {
                    const error = JSON.parse(err.error);
                    const errorMessage = error.detailMessage && error.detailMessage.length ? error.detailMessage : error.exception;
                    if (error.message && errorMessage) {
                        return new errorsActions.ServerErrorCompleteAction({
                            title: error.message,
                            description: errorMessage
                        });
                    }
                } catch (e) { }
            }
            return {
                type: 'NO_ACTION'
            };
        });

    constructor(
        private actions$: Actions,
        private configService: GlobalConfigService
    ) { }
}
