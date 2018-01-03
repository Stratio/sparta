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

import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EgeoModule } from '@stratio/egeo';
import { CrossdataTables } from './crossdata-tables/crossdata-tables.component';
import { CrossdataQueries } from './crossdata-queries/crossdata-queries.component';
import { SharedModule } from '@app/shared';
import { CrossdataComponent } from './crossdata.component';
import { CrossdataRouter } from './crossdata.router';

@NgModule({
   declarations: [
      CrossdataTables,
      CrossdataQueries,
      CrossdataComponent
   ],
   imports: [
      EgeoModule,
      FormsModule,
      CrossdataRouter,
      SharedModule
   ]
})

export class CrossdataModule { }
