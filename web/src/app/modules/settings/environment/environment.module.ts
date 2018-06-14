/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { StBreadcrumbsModule, StTableModule, StCheckboxModule, StModalModule,
    StSearchModule, StInputModule, StModalService } from '@stratio/egeo';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EffectsModule } from '@ngrx/effects';
import { EnvironmentComponent } from './environment.component';
import { EnvironmentEffect } from './effects/environment';
import { reducers } from './reducers/';
import { ImportEnvironmentModalComponent } from './components/import-environment-modal/import-environment-modal.component';
import { SharedModule } from '@app/shared';
import { EnvironmentRoutingModule } from './environment.routes';

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
        EnvironmentRoutingModule,
        SharedModule,
        StSearchModule,
        StBreadcrumbsModule,
        StTableModule,
        StCheckboxModule,
        StInputModule
    ],
    providers: [
      StModalService
   ]
})
export class EnvironmentModule { }
