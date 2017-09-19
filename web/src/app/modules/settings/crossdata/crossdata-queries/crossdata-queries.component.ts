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

import { Component, OnInit, Output, EventEmitter, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { Store } from '@ngrx/store';


import * as fromRoot from 'reducers';
import * as crossdataActions from 'actions/crossdata';

import { Observable } from 'rxjs/Observable';
import { StTableHeader } from '@stratio/egeo';

@Component({
    selector: 'crossdata-queries',
    templateUrl: './crossdata-queries.template.html',
    styleUrls: ['./crossdata-queries.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CrossdataQueries implements OnInit {

    public queryResults$: Observable<any>;
    public sqlQuery: string = '';
    public fields: StTableHeader[] = [];

    public executeQuery() {
        if (this.sqlQuery.length) {
            this.store.dispatch(new crossdataActions.ExecuteQueryAction(this.sqlQuery));
        }
    }

    ngOnInit() {

    }

    constructor(private store: Store<fromRoot.State>) {

    }



}
