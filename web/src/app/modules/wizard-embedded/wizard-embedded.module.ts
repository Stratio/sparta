/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WizardEmbeddedComponent } from '@app/wizard-embedded/wizard-embedded.component';

import { WeHeaderModule } from '@app/wizard-embedded/we-header/we-header.module';
import { WizardEditorModule } from '@app/wizard/components/wizard-editor/wizard-editor.module';
import { SelectedEntityModule } from '@app/wizard/components/selected-entity/selected-entity.module';
import { WeConfigEditorModule } from '@app/wizard-embedded/we-config-editor/we-config-editor.module';
import { WeSettingsEditorModule } from '@app/wizard-embedded/we-settings-editor/we-settings-editor.module';
import {SidebarConfigModule} from '@app/wizard/components/sidebar-config/sidebar-config.module';
import {SpartaSidebarModule} from '@app/shared/components/sparta-sidebar/sparta-sidebar.module';
import {WizardDetailsModule} from '@app/wizard/components/wizard-details/wizard-details.module';

@NgModule({
  exports: [
    WizardEmbeddedComponent
  ],
  declarations: [
    WizardEmbeddedComponent,
  ],
  imports: [
    CommonModule,
    WeHeaderModule,
    WizardEditorModule,
    SelectedEntityModule,
    WeConfigEditorModule,
    WeSettingsEditorModule,
    SidebarConfigModule,

    SpartaSidebarModule,
    WizardDetailsModule
  ]
})

export class WizardEmbeddedModule {}
