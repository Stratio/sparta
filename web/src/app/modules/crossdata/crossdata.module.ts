/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { StoreModule } from '@ngrx/store';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StSearchModule, StSwitchModule, StTableModule, StCheckboxModule,
    StPaginationModule, StBreadcrumbsModule, StProgressBarModule } from '@stratio/egeo';
import { EffectsModule } from '@ngrx/effects';

import { CrossdataTables } from './components/crossdata-tables/crossdata-tables.component';
import { CrossdataQueries } from './components/crossdata-queries/crossdata-queries.component';
import { SharedModule } from '@app/shared';
import { CrossdataComponent } from './crossdata.component';
import { CrossdataRouter } from './crossdata.router';
import { CrossdataEffect } from './effects/crossdata';
import { reducers } from './reducers/';
import { HighlightTextareaModule } from '@app/shared/components/highlight-textarea/hightlight-textarea.module';

@NgModule({
   declarations: [
      CrossdataTables,
      CrossdataQueries,
      CrossdataComponent
   ],
   imports: [
      FormsModule,
      CrossdataRouter,
      EffectsModule.forFeature([CrossdataEffect]),
      StoreModule.forFeature('crossdata', reducers),
      HighlightTextareaModule,
      SharedModule,
      StSearchModule,
      StSwitchModule,
      StTableModule,
      StCheckboxModule,
      StPaginationModule,
      StProgressBarModule,
      StBreadcrumbsModule
   ]
})

export class CrossdataModule { }
