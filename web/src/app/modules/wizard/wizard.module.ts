/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EgeoModule, StModalModule } from '@stratio/egeo';
import {
    WizardComponent, WizardHeaderComponent, WizardConfigEditorComponent,
    WizardEditorComponent, WizardEditorService, WizardNodeComponent,
    DraggableSvgDirective, WizardEdgeComponent, SelectedEntityComponent,
    WizardSettingsComponent
} from '.';
import { WizardRoutingModule } from './wizard.router';
import { SharedModule } from '@app/shared';
import { WizardModalComponent } from './components/wizard-modal/wizard-modal.component';
import { WizardDetailsComponent } from './components/wizard-editor/wizard-details/wizard-details.component';
import { WizardService } from './services/wizard.service';
import { ValidateSchemaService } from './services/validate-schema.service';


@NgModule({
    declarations: [
        WizardComponent,
        WizardHeaderComponent,
        WizardEditorComponent,
        WizardSettingsComponent,
        WizardNodeComponent,
        WizardDetailsComponent,
        DraggableSvgDirective,
        WizardEdgeComponent,
        WizardConfigEditorComponent,
        SelectedEntityComponent,
        WizardModalComponent
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
    providers: [WizardEditorService, WizardService, ValidateSchemaService]
})

export class WizardModule { }
