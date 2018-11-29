/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { AppRouter } from 'app';
import { EgeoModule } from '@stratio/egeo';
import { TranslateModule } from '@ngx-translate/core';
import { StoreModule } from '@ngrx/store';
import { TRANSLATE_CONFIG } from '@app/core';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';

import { reducers } from './reducers';

import { BrowserModule } from '@angular/platform-browser';
import { EffectsModule } from '@ngrx/effects';

import { GlobalEffect } from './effects/global';
import { HttpClientModule } from '@angular/common/http';
import { environment } from '../environments/environment';


export const APP_IMPORTS: Array<any> = [
   AppRouter,
   BrowserModule,
   EgeoModule.forRoot(),
   HttpClientModule,
   TranslateModule.forRoot(TRANSLATE_CONFIG),
   StoreModule.forRoot(reducers),
    !environment.production
      ? StoreDevtoolsModule.instrument({ maxAge: 50 })
      : [],
   EffectsModule.forRoot([
      GlobalEffect
   ])
];
