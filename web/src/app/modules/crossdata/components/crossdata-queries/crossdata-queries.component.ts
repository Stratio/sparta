/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
