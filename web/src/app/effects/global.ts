/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
