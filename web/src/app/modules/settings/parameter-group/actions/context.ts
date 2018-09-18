/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';
import { GlobalParam } from './../models/globalParam';

export const LIST_CONTEXT_PARAMS = '[Context List] List contexts params';
export const LIST_CONTEXT_PARAMS_COMPLETE = '[Context List] List global contexts complete';
export const LIST_CONTEXT_PARAMS_ERROR = '[Context List] List global contexts error';
export const ADD_CONTEXT_PARAMS = '[Context List] Add contexts params';
export const ADD_CONTEXT_PARAMS_COMPLETE = '[Context List] Add contexts params complete';
export const SAVE_CONTEXT_PARAMS = '[Context List Save contexts params';
export const SAVE_CONTEXT_PARAMS_COMPLETE = '[Context List] Save contexts params complete';
export const DELETE_CONTEXT_PARAMS = '[Context List] Delete contexts params';
export const SEARCH_CONTEXT_PARAMS = '[Context List] Search contexts params';

export class ListContextParamsAction implements Action {
   readonly type = LIST_CONTEXT_PARAMS;
   constructor() { }
}

export class ListContextParamsCompleteAction implements Action {
   readonly type = LIST_CONTEXT_PARAMS_COMPLETE;
   constructor(public params: GlobalParam[]) { }
}

export class ListContextParamsErrorAction implements Action {
   readonly type = LIST_CONTEXT_PARAMS_ERROR;
   constructor() { }
}

export class AddContextParamsAction implements Action {
   readonly type = ADD_CONTEXT_PARAMS;
   constructor() { }
}

export class AddContextParamsActionComplete implements Action {
   readonly type = ADD_CONTEXT_PARAMS_COMPLETE;
   constructor() { }
}

export class SaveContextAction implements Action {
   readonly type = SAVE_CONTEXT_PARAMS;
   constructor(public payload: any) { }
}
export class SaveContextActionComplete implements Action {
   readonly type = SAVE_CONTEXT_PARAMS_COMPLETE;
   constructor(public payload: any) { }
}
export class DeleteContextAction implements Action {
   readonly type = DELETE_CONTEXT_PARAMS;
   constructor(public payload: any) { }
}

export class SearchContextAction implements Action {
   readonly type = SEARCH_CONTEXT_PARAMS;
   constructor(public text: string) {}
}


export type Actions = ListContextParamsAction
  | ListContextParamsCompleteAction
  | ListContextParamsErrorAction
  | AddContextParamsAction
  | AddContextParamsActionComplete
  | SaveContextAction
  | SaveContextActionComplete
  | DeleteContextAction
  | SearchContextAction;
