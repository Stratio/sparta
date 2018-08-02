/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StoreModule } from '@ngrx/store';
import { DndListModule } from '@fjsc/ng2-dnd-list';
import { PerfectScrollbarModule } from 'ngx-perfect-scrollbar';
import { StDropdownMenuModule } from '@stratio/egeo';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { NodeSchemaInputBoxComponent } from './components/node-schema-input-box/node-schema-input-box.component';
import { NodeSchemaOutputBoxComponent } from './components/node-schema-output-box/node-schema-output-box.component';
import { QueryBuilderComponent } from './query-builder.component';
import { FieldPositionDirective } from './directives/field-position.directive';
import { NodeSchemaPathComponent } from './components/node-schema-path/node-schema-path.component';
import { NodeJoinPathComponent } from '@app/wizard/components/query-builder/components/node-join-path/node-join-path.component';
import { reducers } from './reducers/';


@NgModule({
  exports: [
    QueryBuilderComponent
  ],
  declarations: [
    NodeSchemaInputBoxComponent,
    NodeSchemaOutputBoxComponent,
    QueryBuilderComponent,
    NodeSchemaPathComponent,
    FieldPositionDirective,
    NodeJoinPathComponent
  ],
  imports: [
    CommonModule,
    DndListModule,
    StoreModule.forFeature('queryBuilder', reducers),
    PerfectScrollbarModule,
    FormsModule,
    TranslateModule,
    StDropdownMenuModule
  ],
})

export class QueryBuilderModule { }
