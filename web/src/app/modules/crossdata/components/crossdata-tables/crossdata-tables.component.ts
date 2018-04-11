/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, ChangeDetectionStrategy, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs/Subscription';
import { StTableHeader, StDropDownMenuItem } from '@stratio/egeo';

import * as fromCrossdata from './../../reducers';
import * as crossdataActions from './../../actions/crossdata';
import { Observable } from 'rxjs/Observable';

@Component({
    selector: 'crossdata-tables',
    templateUrl: './crossdata-tables.template.html',
    styleUrls: ['./crossdata-tables.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CrossdataTables implements OnInit, OnDestroy {
    public tableList$: Observable<any>;
    public selectedDatabaseSubscription: Subscription;
    public loadingTables$: Observable<boolean>;
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
    public searchedTable = '';
    public debounce = 100;
    public minLength = 1;
    public orderBy = '';
    public sortOrder = true;
    public onChangeValue(event: boolean) {
        this.store.dispatch(new crossdataActions.ShowTemporaryTablesAction(event));
    }

    ngOnInit() {
        this.store.dispatch(new crossdataActions.GetDatabasesAction());
        this.getTablesFromDatabase('default');
        this.tableList$ = this.store.select(fromCrossdata.getTablesList);
        this.databaseSubscription = this.store.select(fromCrossdata.getDatabases).subscribe((databases: Array<any>) => {
            this.databases = databases.map((database: any) => {
                return {
                    label: database.name,
                    value: database.name
                };
            });
        });

        /*this.store.select(fromRoot.isLoadingDatabases).subscribe((active: boolean) => {
            console.log(active);
        });*/

        this.loadingTables$ = this.store.select(fromCrossdata.isLoadingTables);

        this.selectedTablesSubscription = this.store.select(fromCrossdata.getSelectedTables).subscribe((tables: Array<string>) => {
            this.selectedTables = tables;
        });


        this.selectedDatabaseSubscription = this.store.select(fromCrossdata.getSelectedDatabase).subscribe((database: string) => {
            if (this.selectedDatabase !== database) {
                this.selectedDatabase = database;
            }
        });
    }

    getTablesFromDatabase(databasename: string) {
        this.store.dispatch(new crossdataActions.ListDatabaseTablesAction(databasename));
    }

    constructor(private store: Store<fromCrossdata.State>, private _cd: ChangeDetectorRef) { }

    onSearchResult(event: any) {
        this.store.dispatch(new crossdataActions.FilterTablesAction(event.text));
    }

    reloadDatabases() {
        this.store.dispatch(new crossdataActions.GetDatabasesAction());
        this.store.dispatch(new crossdataActions.ListDatabaseTablesAction(this.selectedDatabase));
    }

    changeOrder($event: any): void {
        this.store.dispatch(new crossdataActions.ChangeTablesOrderAction({
            orderBy: $event.orderBy,
            sortOrder: $event.type
        }));
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
