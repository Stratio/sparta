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
import { StoreModule } from '@ngrx/store';
import { StBreadcrumbsModule, StTableModule, StCheckboxModule, StModalModule, StSearchModule } from '@stratio/egeo';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EffectsModule } from '@ngrx/effects';
import { EnvironmentComponent } from './environment.component';
import { EnvironmentEffect } from './effects/environment';
import { reducers } from './reducers/';
import { ImportEnvironmentModalComponent } from './components/import-environment-modal/import-environment-modal.component';
import { SharedModule } from '@app/shared';

@NgModule({
    declarations: [
        EnvironmentComponent,
        ImportEnvironmentModalComponent
    ],
    imports: [
        FormsModule,
        StoreModule.forFeature('environment', reducers),
        EffectsModule.forFeature([EnvironmentEffect]),
        StModalModule.withComponents([ImportEnvironmentModalComponent]),
        ReactiveFormsModule,
        SharedModule,
        StSearchModule,
        StBreadcrumbsModule,
        StTableModule,
        StCheckboxModule
    ]
})
export class EnvironmentModule { }
