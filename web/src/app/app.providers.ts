/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ConfigService, INITIALIZER } from '@app/core';
import {
    WorkflowService,
    ApiInterceptor,
    GlobalConfigService,
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
    GlobalConfigService,
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
    InitializeWorkflowService
];
