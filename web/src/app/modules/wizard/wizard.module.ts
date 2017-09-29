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
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EgeoModule, StModalModule } from '@stratio/egeo';
import {
    WizardComponent, WizardHeaderComponent, WizardConfigEditorComponent,
    WizardEditorComponent, WizardEditorService, WizardBoxComponent,
    DraggableSvgDirective, WizardSegmentComponent, SelectedEntityComponent
} from '.';
import { WizardRoutingModule } from './wizard.router';
import { SharedModule } from '@app/shared';
import { DndModule } from 'ng2-dnd';
import { WizardModalComponent } from '@app/wizard/wizard-modal/wizard-modal.component';
import { EntityWriterComponent } from '@app/wizard/wizard-config-editor/entity-writer/entity.writer.component';


@NgModule({
    declarations: [
        WizardComponent,
        WizardHeaderComponent,
        WizardEditorComponent,
        WizardBoxComponent,
        DraggableSvgDirective,
        WizardSegmentComponent,
        WizardConfigEditorComponent,
        SelectedEntityComponent,
        WizardModalComponent,
        EntityWriterComponent
    ],
    imports: [
        EgeoModule.forRoot(),
        StModalModule.withComponents([WizardModalComponent]),
        WizardRoutingModule,
        FormsModule,
        SharedModule,
        FormsModule,
        ReactiveFormsModule
    ],
    providers: [WizardEditorService]
})

export class WizardModule { }
