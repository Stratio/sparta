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
import { StTableHeader, StDropDownMenuItem } from '@stratio/egeo';
import { Subscription } from 'rxjs/Rx';
import { OnDestroy, ChangeDetectorRef } from '@angular/core';


@Component({
    selector: 'crossdata-tables',
    templateUrl: './crossdata-tables.template.html',
    styleUrls: ['./crossdata-tables.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CrossdataTables implements OnInit, OnDestroy {
    public tableList$: Observable<any>;
    public selectedDatabaseSubscription: Subscription;
    public databaseSubscription: Subscription;
    public databases: StDropDownMenuItem[] = [];
    public fields: StTableHeader[] = [
        { id: 'name', label: 'Name' },
        { id: 'database', label: 'Database' },
        { id: 'type', label: 'Type' },
        { id: 'temporary', label: 'Temporary' }
    ];
    public showTemporaryTables = false;
    public selectedDatabase = '';
    public onChangeValue(event: boolean) {
        this.store.dispatch(new crossdataActions.ShowTemporaryTablesAction(event));
    }

    ngOnInit() {
        this.store.dispatch(new crossdataActions.GetDatabasesAction());
        this.getTablesFromDatabase('default');
        this.tableList$ = this.store.select(fromRoot.getTablesList);
        this.databaseSubscription = this.store.select(fromRoot.getDatabases).subscribe((databases: Array<any>) => {
            this.databases = databases.map((database: any) => {
                return {
                    label: database.name,
                    value: database.name
                };
            });
        });

        this.selectedDatabaseSubscription = this.store.select(fromRoot.getSelectedDatabase).subscribe((database: string) => {
            if (this.selectedDatabase !== database) {
                this.selectedDatabase = database;
            }
        });
    }

    getTablesFromDatabase(databasename: string) {
        this.store.dispatch(new crossdataActions.ListDatabaseTablesAction(databasename));
    }

    constructor(private store: Store<fromRoot.State>, private _cd: ChangeDetectorRef) {

    }

    onSearchResult(event: string) {

    }

    ngOnDestroy(): void {
        this.databaseSubscription && this.databaseSubscription.unsubscribe();
        this.selectedDatabaseSubscription && this.selectedDatabaseSubscription.unsubscribe();
    }

}
