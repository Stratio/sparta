/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { StModalService } from '@stratio/egeo';
import { ActivatedRoute, Router } from '@angular/router';

import * as fromRoot from 'reducers';

@Injectable()
export class ManagingService {


    constructor(private store: Store<fromRoot.State>, private _modalService: StModalService, private translate: TranslateService,
        private route: Router, private currentActivatedRoute: ActivatedRoute) {



    }
}
