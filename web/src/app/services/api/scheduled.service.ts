/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Store } from '@ngrx/store';

import * as fromRoot from 'reducers';
import { ApiService } from './api.service';

@Injectable()
export class ScheduledService extends ApiService {

  constructor(private _http: HttpClient, _store: Store<fromRoot.State>) {
    super(_http, _store);
  }
  
  createSheduledExecution(config: any) {
    const options: any = {
      body: config
    };
    return this.request('scheduledWorkflowTasks', 'post', options);
  }

  getScheduledWorkflowTasks() {
    const options: any = {};
    return this.request('scheduledWorkflowTasks', 'get', options);
  }
}
