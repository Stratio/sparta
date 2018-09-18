/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
   StModalModule, StProgressBarModule, StTagInputModule, StFullscreenLayoutModule,
   StHorizontalTabsModule, StModalService, StDropdownMenuModule, EgeoResolveService,
   StTreeModule, StSearchModule, StCheckboxModule
} from '@stratio/egeo';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { PerfectScrollbarModule } from 'ngx-perfect-scrollbar';
import { PERFECT_SCROLLBAR_CONFIG } from 'ngx-perfect-scrollbar';
import { PerfectScrollbarConfigInterface } from 'ngx-perfect-scrollbar';
import {
   WizardComponent, WizardHeaderComponent, WizardConfigEditorComponent,
   WizardEditorComponent, WizardEditorService, WizardNodeComponent,
   DraggableSvgDirective, WizardEdgeComponent, SelectedEntityComponent,
   WizardSettingsComponent
} from '.';
import { WizardRoutingModule } from './wizard.router';
import { SharedModule } from '@app/shared';
import { WizardModalComponent } from './components/wizard-modal/wizard-modal.component';
import { WizardDetailsComponent } from './components/wizard-details/wizard-details.component';
import { WizardService } from './services/wizard.service';
import { ValidateSchemaService } from './services/validate-schema.service';
import { WizardEffect } from './effects/wizard';
import { DebugEffect } from './effects/debug';
import { ExternalDataEffect } from './effects/externalData';

import { reducers } from './reducers/';
import { WizardEditorContainer } from './containers/wizard-editor-container/wizard-editor-container.component';
import { CrossdataModule } from '@app/crossdata/crossdata.module';
import { EdgeOptionsComponent } from '@app/wizard/components/edge-options/edge-options.component';
import { MocksConfigComponent } from '@app/wizard/components/wizard-config-editor/mocks-config/mocks-config.component';
import { HighlightTextareaModule } from '@app/shared/components/highlight-textarea/hightlight-textarea.module';
import { SidebarConfigComponent } from '@app/wizard/components/wizard-config-editor/sidebar-config/sidebar-config.component';
import { SpForegroundNotificationsModule } from '@app/shared/components/sp-foreground-notifications/sp-foreground-notifications.module';
import { NodeErrorsComponent } from '@app/wizard/components/node-errors/node-errors.component';
import { NodeSchemaComponent } from '@app/wizard/components/node-schema/node-schema.component';
import { WizardConsoleComponent } from '@app/wizard/components/wizard-console/wizard-console.component';
import { NodeTreeDataComponent } from '@app/wizard/components/wizard-console/data-node-tree/node-tree-data.component';
import { QueryBuilderModule } from '@app/wizard/components/query-builder/query-builder.module';
import { MenuOptionsListModule } from '@app/shared/components/menu-options-list/menu-options-list.module';
import { CustomExecutionModule } from '@app/custom-execution/custom-execution.module';
import { ParametersGroupSelectorComponent } from './components/wizard-settings/parameters-group-selector/parameters-group-selector.component';


const DEFAULT_PERFECT_SCROLLBAR_CONFIG: PerfectScrollbarConfigInterface = {
   suppressScrollX: true
};

@NgModule({
   declarations: [
      WizardComponent,
      WizardHeaderComponent,
      WizardEditorContainer,
      WizardEditorComponent,
      WizardSettingsComponent,
      WizardNodeComponent,
      WizardDetailsComponent,
      WizardConsoleComponent,
      DraggableSvgDirective,
      WizardEdgeComponent,
      WizardConfigEditorComponent,
      SelectedEntityComponent,
      EdgeOptionsComponent,
      WizardModalComponent,
      MocksConfigComponent,
      NodeErrorsComponent,
      NodeSchemaComponent,
      NodeTreeDataComponent,
      ParametersGroupSelectorComponent,
      SidebarConfigComponent
   ],
   imports: [
      StProgressBarModule,
      StTagInputModule,
      StFullscreenLayoutModule,
      StDropdownMenuModule,
      StSearchModule,
      StCheckboxModule,
      StHorizontalTabsModule,
      SpForegroundNotificationsModule,
      StTreeModule,
      StModalModule.withComponents([WizardModalComponent]),
      StoreModule.forFeature('wizard', reducers),
      EffectsModule.forFeature([DebugEffect, WizardEffect, ExternalDataEffect]),
      HighlightTextareaModule,
      QueryBuilderModule,
      MenuOptionsListModule,
      WizardRoutingModule,
      CustomExecutionModule,
      FormsModule,
      SharedModule,
      FormsModule,
      ReactiveFormsModule,
      CrossdataModule,
      PerfectScrollbarModule
   ],
   providers: [
      WizardEditorService,
      WizardService,
      ValidateSchemaService,
      StModalService,
      EgeoResolveService,
      {
         provide: PERFECT_SCROLLBAR_CONFIG,
         useValue: DEFAULT_PERFECT_SCROLLBAR_CONFIG
      }]
})

export class WizardModule { }
