/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';
import { GlobalParam } from '@app/settings/parameter-group/models/globalParam';

export const LIST_CUSTOM_PARAMS = '[Custom Params] List custom params';
export const LIST_CUSTOM_PARAMS_COMPLETE = '[Custom Params] List custom params complete';
export const LIST_CUSTOM_PARAMS_ERROR = '[Custom Params] List custom params error';
export const NAVIGAGE_TO_LIST = '[Custom Params] Navigate to list';
export const NAVIGAGE_TO_LIST_COMPLETE = '[Custom Params]  Navigate to list complete';
export const SAVE_PARAM = '[Custom Params] Save param';
export const SAVE_PARAM_COMPLETE = '[Custom Params] Save param complete';
export const LIST_CUSTOM_PARAMS_NAME = '[Custom Params] List custom param name';
export const LIST_CUSTOM_PARAMS_NAME_COMPLETE = '[Custom Params] List custom param name complete';
export const ADD_CUSTOM_PARAMS = '[Custom Params] Add custom params';
export const ADD_CUSTOM_PARAMS_COMPLETE = '[Custom Params] Add custom params complete';
export const ADD_CUSTOM_LIST = '[Custom Params] Add custom list';
export const SAVE_CUSTOM_LIST = '[Custom Params] Save custom list';
export const DELETE_CUSTOM_PARAMS = '[Custom Params] Delete custom param';
export const GO_CUSTOM_PARAMS = '[Custom Params] Go custom param';
export const CHANGE_CONTEXT_OPTION = '[Custom Params] Change context option';
export const ADD_CONTEXT = '[Custom Params] Add context';
export const ADD_CONTEXT_COMPLETE = '[Custom Params] Add context complete';
export const SEARCH_CUSTOM_PARAMS = '[Custom Params] Search custom params';

export class ListCustomParamsAction implements Action {
   readonly type = LIST_CUSTOM_PARAMS;
   constructor() {}
}

export class ListCustomParamsCompleteAction implements Action {
   readonly type = LIST_CUSTOM_PARAMS_COMPLETE;
   constructor(public customLists: GlobalParam[]) {}
}

export class ListCustomParamsErrorAction implements Action {
   readonly type = LIST_CUSTOM_PARAMS_ERROR;
   constructor() {}
}

export class NavigateToListAction implements Action {
   readonly type = NAVIGAGE_TO_LIST;
   constructor(public payload: any) {}
}

export class NavigateToListCompleteAction implements Action {
   readonly type = NAVIGAGE_TO_LIST_COMPLETE;
   constructor(public customParameters: any[]) {}
}

export class SaveParam implements Action {
   readonly type = SAVE_PARAM;
   constructor(public payload: any) {}
}
export class SaveParamComplete implements Action {
   readonly type = SAVE_PARAM_COMPLETE;
   constructor(public payload: any) {}
}
export class ListCustomParamsNameAction implements Action {
   readonly type = LIST_CUSTOM_PARAMS_NAME;
   constructor(public payload: any) {}
}

export class ListCustomParamsNameCompleteAction implements Action {
   readonly type = LIST_CUSTOM_PARAMS_NAME_COMPLETE;
   constructor(public payload: any) {}
}

export class AddCustomParamsAction implements Action {
   readonly type = ADD_CUSTOM_PARAMS;
   constructor() {}
}

export class AddCustomParamsActionComplete implements Action {
   readonly type = ADD_CUSTOM_PARAMS_COMPLETE;
   constructor() {}
}

export class AddCustomListAction implements Action {
   readonly type = ADD_CUSTOM_LIST;
   constructor() {}
}

export class SaveCustomListAction implements Action {
   readonly type = SAVE_CUSTOM_LIST;
   constructor(public payload: any) {}
}

export class DeleteCustomAction implements Action {
   readonly type = DELETE_CUSTOM_PARAMS;
   constructor(public payload: any) {}
}
export class GoCustomAction implements Action {
   readonly type = GO_CUSTOM_PARAMS;
   constructor() {}
}

export class ChangeContextOptionAction implements Action {
   readonly type = CHANGE_CONTEXT_OPTION;
   constructor(public context: any) { }
}

export class AddContextAction implements Action {
   readonly type = ADD_CONTEXT;
   constructor(public payload: string) { }
}

export class AddContextCompleteAction implements Action {
   readonly type = ADD_CONTEXT_COMPLETE;
   constructor(public context: any) { }
}

export class SearchCustomAction implements Action {
   readonly type = SEARCH_CUSTOM_PARAMS;
   constructor(public text: string) {}
}

export type Actions = ListCustomParamsAction
  | ListCustomParamsCompleteAction
  | ListCustomParamsErrorAction
  | NavigateToListAction
  | NavigateToListCompleteAction
  | SaveParam
  | SaveParamComplete
  | ListCustomParamsNameAction
  | AddCustomParamsAction
  | AddCustomParamsActionComplete
  | AddCustomListAction
  | SaveCustomListAction
  | DeleteCustomAction
  | GoCustomAction
  | ChangeContextOptionAction
  | AddContextAction
  | AddContextCompleteAction
  | SearchCustomAction;
