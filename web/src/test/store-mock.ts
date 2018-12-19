/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';

export class MockStore<T> extends BehaviorSubject<T> {
   constructor(private _initialState: T) {
      super(_initialState);
   }

   dispatch = (action: Action): void => {
   }

   select = <R>(pathOrMapFn: any, ...paths: string[]): Observable<R> => {
      return map.call(this, pathOrMapFn);
   }
}
