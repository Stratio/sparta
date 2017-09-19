///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Action } from '@ngrx/store';
import { type } from '../utils';

export const actionTypes: any = {
  LIST_INPUT: type('[Input] List inputs'),
  LIST_INPUT_COMPLETE: type('[Input] List inputs complete'),
  SELECT_INPUT: type('[Input] Select input'),
  DESELECT_INPUT: type('[Input] Deselect input'),
  DELETE_INPUT: type('[Input] Delete input'),
  DELETE_INPUT_COMPLETE: type('[Input] Delete input complete'),
  DELETE_INPUT_ERROR: type('[Input] Delete input error'),
  DISPLAY_MODE: type('[Input] change display mode'),
  DUPLICATE_INPUT: type('[Input] duplicate selected input'),
  DUPLICATE_INPUT_COMPLETE: type('[Input] Duplicate input complete'),
  DUPLICATE_INPUT_ERROR: type('[Input] duplicate input error'),
  EDIT_INPUT: type('[Input] Edit input'),
  CREATE_INPUT: type('[Input] Create input'),
  CREATE_INPUT_COMPLETE: type('[Input] Create input complete'),
  CREATE_INPUT_ERROR: type('[Input] Create input error'),
  UPDATE_INPUT: type('[Input] Update input'),
  UPDATE_INPUT_COMPLETE: type('[Input] Update input complete'),
  UPDATE_INPUT_ERROR: type('[Input] Update input error')
};

export class ListInputAction implements Action {
  type: any = actionTypes.LIST_INPUT;

  constructor() { }
}
export class ListInputFailAction implements Action {
  type: any = actionTypes.LIST_INPUT_FAIL;

  constructor(public payload: any) { }
}

export class ListInputCompleteAction implements Action {
  type: any = actionTypes.LIST_INPUT_COMPLETE;

  constructor(public payload: any) { }
}

export class SelectInputAction implements Action {
  type: any = actionTypes.SELECT_INPUT;

  constructor(public payload: any) { }
}

export class DeselectInputAction implements Action {
  type: any = actionTypes.DESELECT_INPUT;

  constructor(public payload: any) { }
}

export class DeleteInputAction implements Action {
  type: any = actionTypes.DELETE_INPUT;

  constructor(public payload: any) { }
}

export class DeleteInputCompleteAction implements Action {
  type: any = actionTypes.DELETE_INPUT_COMPLETE;

  constructor(public payload: any) { }
}

export class DeleteInputErrorAction implements Action {
  type: any = actionTypes.DELETE_INPUT_ERROR;

  constructor(public payload: any) { }
}

export class DisplayModeAction implements Action {
  type: any = actionTypes.DISPLAY_MODE;

  constructor() { }
}

export class ValidateInputNameAction implements Action {
  type: any = actionTypes.VALIDATE_INPUT_NAME;

  constructor(public payload: String) { }
}

export class DuplicateInputAction implements Action {
  type: any = actionTypes.DUPLICATE_INPUT;

  constructor(public payload: any) { }
}

export class DuplicateInputCompleteAction implements Action {
  type: any = actionTypes.DUPLICATE_INPUT_COMPLETE;
}

export class DuplicateInputErrorAction implements Action {
  type: any = actionTypes.DUPLICATE_INPUT_ERROR;

  constructor(public payload: any) { }
}

export class EditInputAction implements Action {
  type: any = actionTypes.EDIT_INPUT;

  constructor(public payload: any) { }
}

export class CreateInputAction implements Action {
  type: any = actionTypes.CREATE_INPUT;

  constructor(public payload: any) { }
}

export class CreateInputCompleteAction implements Action {
  type: any = actionTypes.CREATE_INPUT_COMPLETE;
}

export class CreateInputErrorAction implements Action {
  type: any = actionTypes.CREATE_INPUT_ERROR;

  constructor(public payload: any) { }
}

export class UpdateInputAction implements Action {
  type: any = actionTypes.UPDATE_INPUT;

  constructor(public payload: any) { }
}

export class UpdateInputCompleteAction implements Action {
  type: any = actionTypes.UPDATE_INPUT_COMPLETE;
}

export class UpdateInputErrorAction implements Action {
  type: any = actionTypes.UPDATE_INPUT_ERROR;

  constructor(public payload: any) { }
}

export type Actions =
  ListInputAction |
  ListInputFailAction |
  ListInputCompleteAction |
  SelectInputAction |
  DeselectInputAction |
  DeleteInputAction |
  DeleteInputCompleteAction |
  DeleteInputErrorAction |
  DuplicateInputAction |
  DuplicateInputCompleteAction |
  DuplicateInputErrorAction |
  EditInputAction |
  CreateInputAction |
  CreateInputCompleteAction |
  CreateInputErrorAction |
  UpdateInputAction |
  UpdateInputCompleteAction |
  UpdateInputErrorAction |
  DisplayModeAction;
