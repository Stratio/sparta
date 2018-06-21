/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';

export const SERVER_ERROR = '[Errors] Server error';
export const SERVER_ERROR_COMPLETE = '[Errors] Server error complete';
export const FORBIDDEN_ERROR = '[Errors] Forbidden error';
export const CHANGE_ROUTE = '[Errors] Change route';
export const HTTP_ERROR = '[Errors] Http error';
export const SAVED_DATA_NOTIFICATION = '[Error] Saved data notification';
export const HIDE_SAVED_DATA_NOTIFICATION = '[Error] Hide saved data notification';

export class ServerErrorAction implements Action {
  readonly type = SERVER_ERROR;
  constructor(public payload: any) { }
}

export class ServerErrorCompleteAction implements Action {
  readonly type = SERVER_ERROR_COMPLETE;
  constructor(public payload: any) { }
}

export class ForbiddenErrorAction implements Action {
  readonly type = FORBIDDEN_ERROR;
  constructor(public payload: any) { }
}

export class ChangeRouteAction implements Action {
  readonly type = CHANGE_ROUTE;
}

export class HttpErrorAction implements Action {
  readonly type = HTTP_ERROR;
  constructor(public payload: any) { }
}

export class SavedDataNotificationAction implements Action {
  readonly type = SAVED_DATA_NOTIFICATION;
}

export class HideSavedDataNotificationAction implements Action {
  readonly type = HIDE_SAVED_DATA_NOTIFICATION;
}

export type Actions = ServerErrorAction |
  ForbiddenErrorAction |
  ChangeRouteAction |
  HttpErrorAction |
  SavedDataNotificationAction |
  HideSavedDataNotificationAction |
  ServerErrorCompleteAction;

