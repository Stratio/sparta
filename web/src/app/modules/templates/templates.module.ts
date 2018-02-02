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
import { StBreadcrumbsModule, StTableModule, StCheckboxModule } from '@stratio/egeo';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EffectsModule } from '@ngrx/effects';

import { TemplatesComponent} from './templates.component';
import { TemplatesRoutingModule } from './templates-routing.module';
import { SharedModule } from '@app/shared';

import { InputsComponent } from './components/template-list/inputs.component';
import { OutputsComponent } from './components/template-list/outputs.component';
import { TransformationsComponent } from './components/template-list/transformations.component';
import { CreateInputComponent } from './components/template-creation/create-input.component';
import { CreateOutputComponent } from './components/template-creation/create-output.component';
import { CreateTransformationsComponent } from './components/template-creation/create-transformation.component';
import { reducers } from './reducers';
import { InputEffect} from './effects/input';
import { TransformationEffect} from './effects/transformation';
import { OutputEffect} from './effects/output';

@NgModule({
    declarations: [
        TemplatesComponent,
        InputsComponent,
        OutputsComponent,
        TransformationsComponent,
        CreateInputComponent,
        CreateOutputComponent,
        CreateTransformationsComponent
    ],
    imports: [
        FormsModule,
        StoreModule.forFeature('templates', reducers),
        EffectsModule.forFeature([InputEffect, TransformationEffect, OutputEffect]),
        ReactiveFormsModule,
        TemplatesRoutingModule,
        SharedModule,
        StBreadcrumbsModule,
        StTableModule,
        StCheckboxModule
    ]
})
export class TemplatesModule { }
