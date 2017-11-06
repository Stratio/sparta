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

import { AppRouter } from 'app';
import { EgeoModule, StModalModule } from '@stratio/egeo';
import { SharedModule } from '@app/shared';
import { TranslateModule } from '@ngx-translate/core';
import { StoreModule} from '@ngrx/store';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { TRANSLATE_CONFIG } from '@app/core';
import { reducer, reducers } from './reducers';

import { BrowserModule } from '@angular/platform-browser';
import { WorkflowCreationModal } from '@app/workflows';
import { EffectsModule } from '@ngrx/effects';

import { InputEffect } from './effects/input';
import { WorkflowEffect } from './effects/workflow';
import { BackupsEffect } from './effects/backups';
import { OutputEffect } from './effects/output';
import { ResourcesEffect } from './effects/resources';
import { CrossdataEffect } from './effects/crossdata';
import { WizardEffect } from './effects/wizard';
import { HttpClientModule } from '@angular/common/http';
import { HttpModule } from '@angular/http';

export function instrumentOptions(): any {
   return {
     // monitor: useLogMonitor({ visible: false, position: 'right' })
   };
}
export const APP_IMPORTS: Array<any> = [
        AppRouter,
        BrowserModule,
        EgeoModule.forRoot(),
        HttpClientModule,
        HttpModule,
        SharedModule,
        TranslateModule.forRoot(TRANSLATE_CONFIG),
        StoreModule.forRoot(reducers),
        EffectsModule.forRoot([
                WorkflowEffect,
                InputEffect,
                OutputEffect,
                BackupsEffect,
                ResourcesEffect,
                CrossdataEffect,
                WizardEffect
        ])
];
