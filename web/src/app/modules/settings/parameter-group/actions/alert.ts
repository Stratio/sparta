/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';

export const SHOW_LOADING = '[Loading Params] Show loading params';
export const HIDE_LOADING = '[Loading Params] Hide loading params';
export const SHOW_ALERT = '[Loading Params] Show alert params';
export const HIDE_ALERT = '[Loading Params] Hide alert params';

export class ShowLoadingAction implements Action {
   readonly type = SHOW_LOADING;
   constructor() { }
}

export class HideLoadingAction implements Action {
   readonly type = HIDE_LOADING;
   constructor() { }
}

export class ShowAlertAction implements Action {
   readonly type = SHOW_ALERT;
   constructor(public message: string) { }
}

export class HideAlertAction implements Action {
   readonly type = HIDE_ALERT;
   constructor() { }
}

export type Actions = ShowLoadingAction
  | HideLoadingAction
  | ShowAlertAction
  | HideAlertAction;
