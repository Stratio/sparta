/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, ChangeDetectionStrategy, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import { StTableHeader, StDropDownMenuItem } from '@stratio/egeo';

import * as fromCrossdata from './../../reducers';
import * as crossdataActions from './../../actions/crossdata';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';

@Component({
   selector: 'crossdata-catalog',
   templateUrl: './crossdata-catalog.template.html',
   styleUrls: ['./crossdata-catalog.styles.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class CrossdataCatalogComponent implements OnInit, OnDestroy {
   public tableList$: Observable<any>;
   public loadingTables$: Observable<boolean>;
   public openedTables: Array<string> = [];
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

   private _componentDestroyed = new Subject();

   constructor(private store: Store<fromCrossdata.State>, private _cd: ChangeDetectorRef) { }

   ngOnInit() {
      this.store.dispatch(new crossdataActions.GetDatabasesAction());
      this.getTablesFromDatabase('default');
      this.tableList$ = this.store.select(fromCrossdata.getTablesList);
      this.store.select(fromCrossdata.getDatabases)
         .takeUntil(this._componentDestroyed)
         .subscribe((databases: Array<any>) => {
            this.databases = databases.map((database: any) => {
               return {
                  label: database.name,
                  value: database.name
               };
            });
         });

      this.store.select(fromCrossdata.getOpenedTables)
         .takeUntil(this._componentDestroyed)
         .subscribe((openedTables) => {
            this.openedTables = openedTables;
            this._cd.markForCheck();
         });

      this.loadingTables$ = this.store.select(fromCrossdata.isLoadingTables);

      this.store.select(fromCrossdata.getSelectedTables)
         .takeUntil(this._componentDestroyed)
         .subscribe((tables: Array<string>) => {
            this.selectedTables = tables;
         });


      this.store.select(fromCrossdata.getSelectedDatabase)
         .takeUntil(this._componentDestroyed)
         .subscribe((database: string) => {
            if (this.selectedDatabase !== database) {
               this.selectedDatabase = database;
            }
         });
   }

   onChangeValue(event: boolean) {
      this.store.dispatch(new crossdataActions.ShowTemporaryTablesAction(event));
   }

   getTablesFromDatabase(databasename: string) {
      this.store.dispatch(new crossdataActions.ListDatabaseTablesAction(databasename));
   }

   onSearchResult(event: any) {
      this.store.dispatch(new crossdataActions.FilterTablesAction(event.text));
   }

   reloadDatabases() {
      this.store.dispatch(new crossdataActions.GetDatabasesAction());
      this.store.dispatch(new crossdataActions.ListDatabaseTablesAction(this.selectedDatabase));
   }

   checkValue(event: any) {
      this.store.dispatch(new crossdataActions.SelectTableAction(event));
      this.store.dispatch(new crossdataActions.OpenCrossdataTableAction(event.name));
   }

   ngOnDestroy(): void {
      this._componentDestroyed.next();
      this._componentDestroyed.unsubscribe();
   }
}
