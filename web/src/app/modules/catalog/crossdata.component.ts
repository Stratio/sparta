/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, ChangeDetectionStrategy, ChangeDetectorRef, ViewChild } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';

import { BreadcrumbMenuService } from 'services';
import { CrossdataTables } from './components/crossdata-tables/crossdata-tables.component';
import * as fromRoot from 'reducers';

@Component({
   selector: 'crossdata',
   templateUrl: './crossdata.template.html',
   styleUrls: ['./crossdata.styles.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class CrossdataComponent {

   public sparkUILink$: Observable<string>;
   @ViewChild(CrossdataTables) crossdataTablesChild;

   constructor(public breadcrumbMenuService: BreadcrumbMenuService, private _cd: ChangeDetectorRef,
      private _store: Store<fromRoot.State>) {
      this.breadcrumbOptions = breadcrumbMenuService.getOptions();
      this.sparkUILink$ = this._store.pipe(select(fromRoot.getSparkUILink));
   }

   public options: Array<any> = [
      {
         i: 0,
         id: 'catalog',
         text: 'CATALOG',
         description: 'CATALOG_DESCRIPTION'
      },
      {
         i: 1,
         id: 'queries',
         text: 'QUERIES',
         description: 'QUERIES_DESCRIPTION'
      }
   ];
   public activeMenuOption: any = this.options[0];
   public breadcrumbOptions: Array<any>;


   public onChangedOption(event: any) {
      if (event.id === 'catalog') {
         this.crossdataTablesChild.reloadDatabases();
      }
      this.activeMenuOption = event;
      this._cd.markForCheck();
   }

   public openSparkUI() {
      this.sparkUILink$.pipe(take(1)).subscribe((uiLink: string) => window.open(uiLink, '_blank'));
   }

}
