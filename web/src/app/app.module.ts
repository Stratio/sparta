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

import { NgModule, ApplicationRef } from '@angular/core';


/* App Root */
import { AppComponent } from './app.component';
import { AppState, InternalStateType } from './app.service';
import { Error404Component } from '@app/errors/error-404/error-404.component';
import { APP_IMPORTS } from 'app/app.imports';
import { APP_PROVIDERS } from './app.providers';
import { INITIALIZER } from '@app/core';

type StoreType = {
   state: InternalStateType,
   restoreInputValues: () => void,
   disposeOldHosts: () => void
};

@NgModule({
    bootstrap: [AppComponent],
    declarations: [AppComponent, Error404Component],
    imports: [
        ...APP_IMPORTS
    ],
    providers: [
        ...APP_PROVIDERS,
        INITIALIZER
    ]
})
export class AppModule {
  constructor(
      public appRef: ApplicationRef,
      public appState: AppState
   ) {

   }

   public hmrOnInit(store: StoreType): void {
      if (!store || !store.state) {
         return;
      }
      // set state
      this.appState._state = store.state;
      // set input values
      if ('restoreInputValues' in store) {
         let restoreInputValues: any = store.restoreInputValues;
         setTimeout(restoreInputValues);
      }

      this.appRef.tick();
      delete store.state;
      delete store.restoreInputValues;
   }

   public hmrOnDestroy(store: StoreType): void {
      const cmpLocation: any = this.appRef.components.map((cmp: any) => cmp.location.nativeElement);
      // save state
      const state: any = this.appState._state;
      store.state = state;
   }

   public hmrAfterDestroy(store: StoreType): void {
      // display new elements
      store.disposeOldHosts();
      delete store.disposeOldHosts;
   }
}
