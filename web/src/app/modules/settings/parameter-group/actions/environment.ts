/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';
import { GlobalParam } from '@app/settings/parameter-group/models/globalParam';

export const LIST_ENVIRONMENT_PARAMS = '[Environment Params] List environment params';
export const LIST_ENVIRONMENT_PARAMS_COMPLETE = '[Environment Params] List environment params complete';
export const LIST_ENVIRONMENT_PARAMS_ERROR = '[Environment Params] List environment params error';
export const ADD_CONTEXT = '[Environment Params] Add context';
export const ADD_CONTEXT_COMPLETE = '[Environment Params] Add context complete';
export const SAVE_PARAM = '[Environment Params] Save param';
export const SAVE_PARAM_COMPLETE = '[Environment Params] Save param complete';
export const UPDATE_PARAM = '[Environment Params] Update param';
export const ADD_ENVIRONMENT_PARAMS = '[Environment Params] Add environment params';
export const ADD_ENVIRONMENT_PARAMS_COMPLETE = '[Environment Params] Add environment params complete';
export const DELETE_ENVIRONMENT_PARAMS = '[Environment Params] Delete environment params';
export const CHANGE_CONTEXT_OPTION = '[Environment Params] Change context option';
export const SEARCH_ENVIRONMENT_PARAMS = '[Environment Params] Search environment params';
export const ADD_ENVIRONMENT_CONTEXT = '[Environment Params] Add environment context';
export const SAVE_ENVIRONMENT_CONTEXT = '[Environment Params] Save environment context';
export const SAVE_ENVIRONMENT_CONTEXT_COMPLETE = '[Environment Params] Save environment context complete';
export const DELETE_ENVIRONMENT_CONTEXT = '[Environment Params] Delete environment context';
export const EXPORT_ENVIRONMENT_PARAMS = '[Environment Params] Export environment params';
export const EXPORT_ENVIRONMENT_PARAMS_COMPLETE = '[Environment Params] Export environment params complete';
export const EXPORT_ENVIRONMENT_PARAMS_ERROR = '[Environment Params] Export environment params error';

export class ListEnvironmentParamsAction implements Action {
   readonly type = LIST_ENVIRONMENT_PARAMS;
   constructor() { }
}

export class ListEnvironmentParamsCompleteAction implements Action {
   readonly type = LIST_ENVIRONMENT_PARAMS_COMPLETE;
   constructor(public params: GlobalParam[]) { }
}

export class ListEnvironmentParamsErrorAction implements Action {
   readonly type = LIST_ENVIRONMENT_PARAMS_ERROR;
   constructor() { }
}

export class AddContextAction implements Action {
   readonly type = ADD_CONTEXT;
   constructor(public payload: string) { }
}

export class AddContextCompleteAction implements Action {
   readonly type = ADD_CONTEXT_COMPLETE;
   constructor(public context: any) { }
}

export class SaveParam implements Action {
   readonly type = SAVE_PARAM;
   constructor(public payload: any) { }
}
export class SaveParamComplete implements Action {
   readonly type = SAVE_PARAM_COMPLETE;
   constructor(public payload: any) { }
}

export class UpdateParam implements Action {
   readonly type = UPDATE_PARAM;
   constructor(public payload: any) { }
}

export class AddEnvironmentParamsAction implements Action {
   readonly type = ADD_ENVIRONMENT_PARAMS;
   constructor() { }
}

export class AddEnvironmentParamsActionComplete implements Action {
   readonly type = ADD_ENVIRONMENT_PARAMS_COMPLETE;
   constructor() { }
}

export class DeleteEnviromentAction implements Action {
   readonly type = DELETE_ENVIRONMENT_PARAMS;
   constructor(public payload: any) { }
}


export class ChangeContextOptionAction implements Action {
   readonly type = CHANGE_CONTEXT_OPTION;
   constructor(public context: any) { }
}

export class SearchEnvironmentAction implements Action {
   readonly type = SEARCH_ENVIRONMENT_PARAMS;
   constructor(public text: string) {}
}

export class AddEnvironmentContextAction implements Action {
   readonly type = ADD_ENVIRONMENT_CONTEXT;
   constructor() {}
}

export class SaveEnvironmentContext implements Action {
   readonly type = SAVE_ENVIRONMENT_CONTEXT;
   constructor(public payload: any) {}
}

export class SaveEnvironmentContextComplete implements Action {
   readonly type = SAVE_ENVIRONMENT_CONTEXT_COMPLETE;
   constructor(public payload: any) {}
}

export class DeleteContextAction implements Action {
   readonly type = DELETE_ENVIRONMENT_CONTEXT;
   constructor(public context: any) {}
}

export class ExportEnvironmentParamsAction implements Action {
    readonly type = EXPORT_ENVIRONMENT_PARAMS;
    constructor() {}
  }
  
  export class ExportEnvironmentParamsCompleteAction implements Action {
    readonly type = EXPORT_ENVIRONMENT_PARAMS_COMPLETE;
    constructor() {}
  }
  
  export class ExportEnvironmentParamsErrorAction implements Action {
    readonly type = EXPORT_ENVIRONMENT_PARAMS_ERROR;
    constructor() {}
  }

export type Actions = ListEnvironmentParamsAction
  | ListEnvironmentParamsCompleteAction
  | ListEnvironmentParamsErrorAction
  | AddContextAction
  | AddContextCompleteAction
  | SaveParam
  | SaveParamComplete
  | UpdateParam
  | AddEnvironmentParamsAction
  | AddEnvironmentParamsActionComplete
  | DeleteEnviromentAction
  | ChangeContextOptionAction
  | SearchEnvironmentAction
  | AddEnvironmentContextAction
  | DeleteContextAction
  | ExportEnvironmentParamsAction
  | ExportEnvironmentParamsCompleteAction
  | ExportEnvironmentParamsErrorAction;
