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

import { ConfigService, INITIALIZER } from '@app/core';
import {
    WorkflowService,
    ApiInterceptor,
    TemplatesService,
    BackupService,
    EnvironmentService,
    ResourcesService,
    ApiService,
    ErrorMessagesService,
    CrossdataService,
    BreadcrumbMenuService,
    InitializeSchemaService,
    InitializeWorkflowService,
    ValidateSchemaService
} from 'services';
import { AppState } from './app.service';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

export const APP_PROVIDERS: Array<any> = [
    AppState,
    ConfigService,
    INITIALIZER,
     {
      provide: HTTP_INTERCEPTORS,
      useClass: ApiInterceptor,
      multi: true
    },
    WorkflowService,
    TemplatesService,
    BackupService,
    EnvironmentService,
    CrossdataService,
    ResourcesService,
    InitializeSchemaService,
    BreadcrumbMenuService,
    ApiService,
    ErrorMessagesService,
    InitializeWorkflowService,
    ValidateSchemaService
];
