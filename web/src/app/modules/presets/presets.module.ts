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
import { PresetsComponent} from './presets.component';
import { PresetsRoutingModule } from './presets-routing.module';
import { SharedModule } from '@app/shared';
import { EgeoModule, StModalModule } from '@stratio/egeo';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { InputsComponent } from './template-list/inputs.component';
import { OutputsComponent } from './template-list/outputs.component';
import { CreateInputComponent } from './template-creation/create-input.component';
import { CreateOutputComponent } from './template-creation/create-output.component';
import { TemplatesBaseComponent } from './template-list/templates-base.component';

@NgModule({
    declarations: [
        PresetsComponent,
        InputsComponent,
        OutputsComponent,
        CreateInputComponent,
        CreateOutputComponent,
    ],
    imports: [
        EgeoModule.forRoot(),
        FormsModule,
        ReactiveFormsModule,
        SharedModule,
        PresetsRoutingModule,
        SharedModule,
    ]
})
export class PresetsModule { }
