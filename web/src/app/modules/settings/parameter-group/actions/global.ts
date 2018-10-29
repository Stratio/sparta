/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Action } from '@ngrx/store';
import { GlobalParam } from './../models/globalParam';

export const LIST_GLOBAL_PARAMS = '[Global Params] List global params';
export const LIST_GLOBAL_PARAMS_COMPLETE = '[Global Params] List global params complete';
export const LIST_GLOBAL_PARAMS_ERROR = '[Global Params] List global params error';
export const ADD_GLOBAL_PARAMS = '[Global Params] Add global params';
export const ADD_GLOBAL_PARAMS_COMPLETE = '[Global Params] Add global params complete';
export const SAVE_GLOBAL_PARAMS = '[Global Params] Save global params';
export const SAVE_GLOBAL_PARAMS_COMPLETE = '[Global Params] Save global params complete';
export const DELETE_GLOBAL_PARAMS = '[Global Params] Delete global params';
export const SEARCH_GLOBAL_PARAMS = '[Global Params] Search global params';
export const DELETE_NEW_GLOBAL_PARAMS = '[Global Params] Delete new global params';
export const EXPORT_GLOBAL_PARAMS = '[Global Params] Export global params';
export const EXPORT_GLOBAL_PARAMS_COMPLETE = '[Global Params] Export global params complete';
export const EXPORT_GLOBAL_PARAMS_ERROR = '[Global Params] Export global params error';
export const IMPORT_GLOBAL_PARAMS = '[Global Params] Import global params';
export const IMPORT_GLOBAL_PARAMS_COMPLETE = '[Global Params] Import global params complete';
export const IMPORT_GLOBAL_PARAMS_ERROR = '[Global Params] Import global params error';

export class ListGlobalParamsAction implements Action {
   readonly type = LIST_GLOBAL_PARAMS;
   constructor() { }
}

export class ListGlobalParamsCompleteAction implements Action {
   readonly type = LIST_GLOBAL_PARAMS_COMPLETE;
   constructor(public params: GlobalParam[]) { }
}

export class ListGlobalParamsErrorAction implements Action {
   readonly type = LIST_GLOBAL_PARAMS_ERROR;
   constructor() { }
}

export class AddGlobalParamsAction implements Action {
   readonly type = ADD_GLOBAL_PARAMS;
   constructor() { }
}

export class AddGlobalParamsActionComplete implements Action {
   readonly type = ADD_GLOBAL_PARAMS_COMPLETE;
   constructor() { }
}

export class SaveGlobalAction implements Action {
   readonly type = SAVE_GLOBAL_PARAMS;
   constructor(public payload: any) { }
}
export class SaveGlobalActionComplete implements Action {
   readonly type = SAVE_GLOBAL_PARAMS_COMPLETE;
   constructor(public payload: any) { }
}
export class DeleteGlobalAction implements Action {
   readonly type = DELETE_GLOBAL_PARAMS;
   constructor(public payload: any) { }
}

export class SearchGlobalAction implements Action {
   readonly type = SEARCH_GLOBAL_PARAMS;
   constructor(public text: string) {}
}

export class DeleteNewGlobalParamsAction implements Action {
    readonly type = DELETE_NEW_GLOBAL_PARAMS;
    constructor() { }
 }

 export class ExportGlobalParamsAction implements Action {
    readonly type = EXPORT_GLOBAL_PARAMS;
    constructor() {}
  }

  export class ExportGlobalParamsCompleteAction implements Action {
    readonly type = EXPORT_GLOBAL_PARAMS_COMPLETE;
    constructor() {}
  }

  export class ExportGlobalParamsErrorAction implements Action {
    readonly type = EXPORT_GLOBAL_PARAMS_ERROR;
    constructor() {}
  }

  export class ImportGlobalParamsAction implements Action {
    readonly type = IMPORT_GLOBAL_PARAMS;
    constructor(public payload: any) { }
}

  export class ImportGlobalParamsCompleteAction implements Action {
    readonly type = IMPORT_GLOBAL_PARAMS_COMPLETE;
    constructor() {}
  }

  export class ImportGlobalParamsErrorAction implements Action {
    readonly type = IMPORT_GLOBAL_PARAMS_ERROR;
    constructor() {}
  }

export type Actions = ListGlobalParamsAction
  | ListGlobalParamsCompleteAction
  | ListGlobalParamsErrorAction
  | SaveGlobalAction
  | SaveGlobalActionComplete
  | DeleteGlobalAction
  | SearchGlobalAction
  | DeleteNewGlobalParamsAction
  | ExportGlobalParamsAction
  | ExportGlobalParamsCompleteAction
  | ExportGlobalParamsErrorAction
  | ImportGlobalParamsAction
  | ImportGlobalParamsCompleteAction
  | ImportGlobalParamsErrorAction;
