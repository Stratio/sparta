/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';

export type InternalStateType = {
   [key: string]: any
};

@Injectable()
export class AppState {

   public _state: InternalStateType = {};

   // already return a clone of the current state
   public get state(): any {
      return this._state = this._clone(this._state);
   }
   // never allow mutation
   public set state(value: any) {
      throw new Error('do not mutate the `.state` directly');
   }

   public get(prop?: any): any {
      // use our state getter for the clone
      const state: any = this.state;
      return state.hasOwnProperty(prop) ? state[prop] : state;
   }

   public set(prop: string, value: any): any {
      // internally mutate our state
      return this._state[prop] = value;
   }

   private _clone(object: InternalStateType): any {
      // simple object clone
      return JSON.parse(JSON.stringify(object));
   }
}
