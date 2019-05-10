/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { StoreModule } from '@ngrx/store';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import {
  StSearchModule, StSwitchModule, StTableModule, StCheckboxModule,
  StPaginationModule, StBreadcrumbsModule, StProgressBarModule
} from '@stratio/egeo';
import { EffectsModule } from '@ngrx/effects';
import { TranslateModule } from '@ngx-translate/core';

import { SpPipesModule, HighlightTextareaModule, LoadingSpinnerModule, SpSelectModule, ToolBarModule } from '@app/shared';

import { CrossdataTables } from './components/crossdata-tables/crossdata-tables.component';
import { CrossdataQueries } from './components/crossdata-queries/crossdata-queries.component';
import { CrossdataComponent } from './crossdata.component';
import { CrossdataRouter } from './crossdata.router';
import { CrossdataEffect } from './effects/crossdata';
import { reducers } from './reducers/';
import { CrossdataCatalogComponent } from './components/crossdata-catalog/crossdata-catalog.component';
import { SpFooterModule } from '@app/shared/components/sp-footer/sp-footer.module';

@NgModule({
  exports: [
    CrossdataCatalogComponent
  ],
  declarations: [
    CrossdataTables,
    CrossdataQueries,
    CrossdataComponent,
    CrossdataCatalogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    CrossdataRouter,
    EffectsModule.forFeature([CrossdataEffect]),
    StoreModule.forFeature('crossdata', reducers),
    SpPipesModule,
    HighlightTextareaModule,
    LoadingSpinnerModule,
    SpFooterModule,
    StSearchModule,
    StSwitchModule,
    StTableModule,
    StCheckboxModule,
    StPaginationModule,
    StProgressBarModule,
    StBreadcrumbsModule,
    SpSelectModule,
    ToolBarModule,
    TranslateModule
  ]
})

export class CrossdataModule { }
