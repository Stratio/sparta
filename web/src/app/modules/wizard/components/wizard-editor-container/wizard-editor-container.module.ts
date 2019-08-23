
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { WizardEditorContainer } from './wizard-editor-container.component';
import { WizardHeaderModule } from './wizard-header/wizard-header.module';

import { WizardDetailsModule } from '@app/wizard/components/wizard-details/wizard-details.module';
import { EdgeOptionsModule } from '@app/wizard/components/edge-options/edge-options.module';
import { WizardAnnotationModule } from '@app/shared/wizard/components/wizard-annotation/wizard-annotation.module';
import { NotificationAlertModule, SpartaSidebarModule } from '@app/shared';
import { WizardEditorModule } from './wizard-editor/wizard-editor.module';
import { InitializeStepService } from '@app/wizard/services/initialize-step.service';
import { SelectedEntityModule } from '../selected-entity/selected-entity.module';

@NgModule({
  exports: [
    WizardEditorContainer
  ],
  declarations: [
    WizardEditorContainer,
  ],
  imports: [
    CommonModule,
    EdgeOptionsModule,
    NotificationAlertModule,
    SelectedEntityModule,
    SpartaSidebarModule,
    WizardAnnotationModule,
    WizardDetailsModule,
    WizardEditorModule,
    WizardHeaderModule
  ],
  providers: [
    InitializeStepService
  ]
})

export class WizardEditorContainerModule {}
