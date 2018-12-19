/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WizardEditorComponent } from './wizard-editor.component';

import { GraphEditorModule } from '@app/shared/components/graph-editor/graph-editor.module';
import { InitializeStepService } from '@app/wizard/services/initialize-step.service';
import { WizardEdgeModule } from '@app/wizard/components/wizard-edge/wizard-edge.module';
import { WizardNodeModule } from '@app/wizard/components/wizard-node/wizard-node.module';
import { WizardSelectorModule } from '@app/wizard/components/wizard-selector/wizard-selector.module';

@NgModule({
  exports: [
    WizardEditorComponent
  ],
  declarations: [
    WizardEditorComponent
  ],
  imports: [
    CommonModule,
    GraphEditorModule,
    WizardEdgeModule,
    WizardNodeModule,
    WizardSelectorModule
  ],
  providers: [
    InitializeStepService
  ]
})

export class WizardEditorModule {}
