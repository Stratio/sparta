/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StModalModule, StModalService} from '@stratio/egeo';
import { EffectsModule } from '@ngrx/effects';
import { TranslateModule } from '@ngx-translate/core';
import { StoreModule } from '@ngrx/store';

import { WizardRoutingModule } from './wizard.router';
import { WizardModalComponent } from './components/wizard-modal/wizard-modal.component';
import { WizardService } from './services/wizard.service';
import { ValidateSchemaService } from './services/validate-schema.service';
import { WizardEffect } from './effects/wizard';
import { DebugEffect } from './effects/debug';
import { ExternalDataEffect } from './effects/externalData';

import { reducers } from './reducers/';
import { CustomExecutionModule } from '@app/custom-execution/custom-execution.module';

import { WizardEmbeddedModule } from '@app/wizard-embedded/wizard-embedded.module';
import { SelectedEntityModule } from '@app/wizard/components/selected-entity/selected-entity.module';
import {WizardNotificationsModule} from '@app/wizard/components/wizard-notifications/wizard-notifications.module';

import { WizardEditorContainerModule } from './components/wizard-editor-container/wizard-editor-container.module';
import { WizardSettingsModule } from './components/wizard-settings/wizard-settings.module';
import { WizardConfigEditorModule } from './components/wizard-config-editor/wizard-config-editor.module';
import { WizardComponent } from './wizard.component';


@NgModule({
  declarations: [
    WizardComponent,
    WizardModalComponent
  ],
  imports: [
    CommonModule,
    StModalModule.withComponents([WizardModalComponent]),
    StoreModule.forFeature('wizard', reducers),
    EffectsModule.forFeature([DebugEffect, WizardEffect, ExternalDataEffect]),
    WizardRoutingModule,
    CustomExecutionModule,
    WizardConfigEditorModule,
    WizardEmbeddedModule,
    WizardEditorContainerModule,
    SelectedEntityModule,
    TranslateModule,
    WizardNotificationsModule,
    WizardSettingsModule
  ],
  providers: [
    WizardService,
    ValidateSchemaService,
    StModalService
  ]
})

export class WizardModule { }
