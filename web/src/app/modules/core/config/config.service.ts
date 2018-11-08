/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Store } from '@ngrx/store';
import { Injectable } from '@angular/core';

import * as fromRoot from 'reducers';
import * as userActions from 'actions/user';
import { GlobalConfigService } from 'app/services';

@Injectable()
export class ConfigService {

    constructor(private configService: GlobalConfigService, private _store: Store<fromRoot.State>) { }

    load(): Promise<any> {
        return this.configService.getConfig()
            .toPromise()
            .then((data: any) => this._store.dispatch(new userActions.GetUserProfileCompleteAction(data)))
            .catch((err: any) => Promise.resolve());
    }
}
