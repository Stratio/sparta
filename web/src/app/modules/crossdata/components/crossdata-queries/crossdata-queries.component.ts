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

import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs/Rx';
import { Observable } from 'rxjs/Observable';
import { StTableHeader } from '@stratio/egeo';

import * as fromCrossdata from './../../reducers';
import * as crossdataActions from './../../actions/crossdata';



@Component({
    selector: 'crossdata-queries',
    templateUrl: './crossdata-queries.template.html',
    styleUrls: ['./crossdata-queries.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CrossdataQueries implements OnInit, OnDestroy {

    public sqlQuery = '';
    public fields: StTableHeader[] = [];
    public results: any[] = [];
    public queryError = '';
    public queryResultSubscription: Subscription;
    public queryErrorSubscription: Subscription;
    public isLoadingQuery$: Observable<boolean>;
    public currentPage = 1;
    public perPage = 10;
        public perPageOptions: any = [
      { value: 10, showFrom: 0 }, { value: 20, showFrom: 0 }, { value: 40, showFrom: 0 }
    ];

    public showResult = false;

    public executeQuery() {
        this.fields = [];
        this.showResult = false;
        this.results = [];
        this.currentPage = 1;
        if (this.sqlQuery.length) {
            this.store.dispatch(new crossdataActions.ExecuteQueryAction(this.sqlQuery));
        }
    }


    public changePage($event: any) {
        this.perPage = $event.perPage;
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
        this.isLoadingQuery$ = this.store.select(fromCrossdata.isLoadingQuery);
        this.queryResultSubscription = this.store.select(fromCrossdata.getQueryResult).subscribe((result: any) => {
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
            this._cd.markForCheck();
        });

        this.queryErrorSubscription = this.store.select(fromCrossdata.getQueryError).subscribe((error: any) => {
            this.queryError = error;
            this._cd.markForCheck();
        });
    }

    constructor(private store: Store<fromCrossdata.State>, private _cd: ChangeDetectorRef) { }

    ngOnDestroy(): void {
        this.queryResultSubscription && this.queryResultSubscription.unsubscribe();
        this.queryErrorSubscription && this.queryErrorSubscription.unsubscribe();
    }
}
