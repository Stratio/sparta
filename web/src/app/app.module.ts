/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
