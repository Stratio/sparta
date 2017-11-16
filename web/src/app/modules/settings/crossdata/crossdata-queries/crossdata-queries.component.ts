import { OnDestroy } from '@angular/core/core';
import { Subscription } from 'rxjs/Rx';
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

import { Component, OnInit, Output, EventEmitter, ViewChild, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
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
export class CrossdataQueries implements OnInit, OnDestroy {

    public sqlQuery: string = '';
    public fields: StTableHeader[] = [];
    public results: any[] = [];
    public queryError: string = '';
    public queryResultSubscription: Subscription;
    public queryErrorSubscription: Subscription;
    public currentPage = 1;
    public perPage = 10;

    public showResult = false;

    public executeQuery() {
        this.fields = [];
        this.results = [];
        this.currentPage = 1;
        if (this.sqlQuery.length) {
            this.store.dispatch(new crossdataActions.ExecuteQueryAction(this.sqlQuery));
        }
    }


    public changePage($event: any) {
        this.currentPage = $event.currentPage;
    }

    public toString(value: any) {
        if (typeof value === 'object') {
            return JSON.stringify(value);
        } else {
            return value;
        }
    }

    ngOnInit() {
        this.queryResultSubscription = this.store.select(fromRoot.getQueryResult).subscribe((result: any) => {
            if (result) {
                this.showResult = true;
            }
            if (result && result.length) {
                const row = result[0];
                const fields: StTableHeader[] = [];
                Object.keys(row).forEach(key => {
                    fields.push({
                        id: key,
                        label: key,
                        sortable: false
                    });
                });
                this.fields = fields;
                this.results = result;
            }
            this._cd.detectChanges();
        });

        this.queryErrorSubscription = this.store.select(fromRoot.getQueryError).subscribe((error: any) => {
            this.queryError = error;
            this._cd.detectChanges();
        });
    }

    constructor(private store: Store<fromRoot.State>, private _cd: ChangeDetectorRef) { }

    ngOnDestroy(): void {
        this.queryResultSubscription && this.queryResultSubscription.unsubscribe();
        this.queryErrorSubscription && this.queryErrorSubscription.unsubscribe();
    }
}
