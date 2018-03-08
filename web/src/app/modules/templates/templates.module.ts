/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { StBreadcrumbsModule, StTableModule, StCheckboxModule, StRadioMenuModule, StRadioModule, 
    StPaginationModule, StProgressBarModule } from '@stratio/egeo';
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
import { TableNotificationModule } from '@app/shared/components/table-notification/table-notification.module';

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
        TableNotificationModule,
        StRadioMenuModule,
        StProgressBarModule,
        StPaginationModule,
        StRadioModule,
        StBreadcrumbsModule,
        StTableModule,
        StCheckboxModule
    ]
})
export class TemplatesModule { }
