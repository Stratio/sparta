/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';

export const GET_USER_PROFILE = '[User] Get user profile';
export const GET_USER_PROFILE_COMPLETE = '[User] Get user profile complete';
export const GET_USER_PROFILE_ERROR = '[User] Get user profile error';
export const SET_EDIT_MONITORING_MODE = '[User] Set edit monitoring mode';

export class GetUserProfileAction implements Action {
  readonly type = GET_USER_PROFILE;

  constructor() { }
}
export class GetUserProfileCompleteAction implements Action {
  readonly type = GET_USER_PROFILE_COMPLETE;

  constructor(public payload: any) { }
}

export class GetUserProfileErrorAction implements Action {
  readonly type = GET_USER_PROFILE_ERROR;

  constructor(public payload: any) { }
}

export class SetEditMonitoringModeAction implements Action {
  readonly type = SET_EDIT_MONITORING_MODE;
    constructor(public payload: any) { }
}

export type Actions =
  GetUserProfileAction |
  GetUserProfileCompleteAction |
  GetUserProfileErrorAction |
  SetEditMonitoringModeAction;

