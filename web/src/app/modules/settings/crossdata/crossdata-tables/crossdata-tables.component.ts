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
import { Observable, SubscribableOrPromise } from 'rxjs/Observable';
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
    public selectedTablesSubscription: Subscription;
    public databases: StDropDownMenuItem[] = [];
    public fields: StTableHeader[] = [
        { id: 'order', label: '', sortable: false },
        { id: 'name', label: 'Name' },
        { id: 'database', label: 'Database' },
        { id: 'type', label: 'Type' },
        { id: 'temporary', label: 'Temporary' }
    ];
    public showTemporaryTables = false;
    public selectedDatabase = '';
    public selectedTables: Array<string> = [];
    public orderBy = '';
    public sortOrder = true;
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

        this.selectedTablesSubscription = this.store.select(fromRoot.getSelectedTables).subscribe((tables: Array<string>) => {
            this.selectedTables = tables;
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

    constructor(private store: Store<fromRoot.State>, private _cd: ChangeDetectorRef) { }

    onSearchResult(event: string) {

    }

    reloadDatabases() {
        this.store.dispatch(new crossdataActions.GetDatabasesAction());
        this.store.dispatch(new crossdataActions.ListDatabaseTablesAction(this.selectedDatabase));
    }

    changeOrder($event: any): void {
        this.orderBy = $event.orderBy;
        this.sortOrder = $event.type;
    }

    checkRow(isChecked: boolean, value: any) {
        this.checkValue({
            checked: isChecked,
            value: value
        });
    }

    checkValue($event: any) {
        if ($event.checked) {
            this.store.dispatch(new crossdataActions.SelectTableAction($event.value));
        } else {
            this.store.dispatch(new crossdataActions.UnselectTableAction($event.value));
        }
    }

    ngOnDestroy(): void {
        this.databaseSubscription && this.databaseSubscription.unsubscribe();
        this.selectedDatabaseSubscription && this.selectedDatabaseSubscription.unsubscribe();
    }

}
