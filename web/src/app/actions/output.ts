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
  LIST_OUTPUT: type('[Output] List outputs'),
  LIST_OUTPUT_COMPLETE: type('[Output] List outputs complete'),
  SELECT_OUTPUT: type('[Output] Select output'),
  DESELECT_OUTPUT: type('[Output] Deselect output'),
  DELETE_OUTPUT: type('[Output] Delete output'),
  DELETE_OUTPUT_COMPLETE: type('[Output] Delete output complete'),
  DELETE_OUTPUT_ERROR: type('[Output] Delete output error'),
  DISPLAY_MODE: type('[Output] change display mode'),
  DUPLICATE_OUTPUT: type('[Output] duplicate selected output'),
  DUPLICATE_OUTPUT_COMPLETE: type('[Output] Duplicate output complete'),
  DUPLICATE_OUTPUT_ERROR: type('[Output] duplicate output error'),
  EDIT_OUTPUT: type('[Output] Edit output'),
  CREATE_OUTPUT: type('[Output] Create output'),
  CREATE_OUTPUT_COMPLETE: type('[Output] Create output complete'),
  CREATE_OUTPUT_ERROR: type('[Output] Create output error'),
  UPDATE_OUTPUT: type('[Output] Update output'),
  UPDATE_OUTPUT_COMPLETE: type('[Output] Update output complete'),
  UPDATE_OUTPUT_ERROR: type('[Output] Update output error'),
  RESET_OUTPUT_FORM: type('[Output] Reset output form')
};

export class ListOutputAction implements Action {
  type: any = actionTypes.LIST_OUTPUT;

  constructor() { }
}
export class ListOutputFailAction implements Action {
  type: any = actionTypes.LIST_OUTPUT_FAIL;

  constructor(public payload: any) { }
}

export class ListOutputCompleteAction implements Action {
  type: any = actionTypes.LIST_OUTPUT_COMPLETE;

  constructor(public payload: any) { }
}

export class SelectOutputAction implements Action {
  type: any = actionTypes.SELECT_OUTPUT;

  constructor(public payload: any) { }
}

export class DeselectOutputAction implements Action {
  type: any = actionTypes.DESELECT_OUTPUT;

  constructor(public payload: any) { }
}

export class DeleteOutputAction implements Action {
  type: any = actionTypes.DELETE_OUTPUT;

  constructor(public payload: any) { }
}

export class DeleteOutputCompleteAction implements Action {
  type: any = actionTypes.DELETE_OUTPUT_COMPLETE;

  constructor(public payload: any) { }
}

export class DeleteOutputErrorAction implements Action {
  type: any = actionTypes.DELETE_OUTPUT_ERROR;

  constructor(public payload: any) { }
}

export class DisplayModeAction implements Action {
  type: any = actionTypes.DISPLAY_MODE;
}

export class ValidateOutputNameAction implements Action {
  type: any = actionTypes.VALIDATE_OUTPUT_NAME;

  constructor(public payload: String) { }
}

export class DuplicateOutputAction implements Action {
  type: any = actionTypes.DUPLICATE_OUTPUT;

  constructor(public payload: any) { }
}

export class DuplicateOutputCompleteAction implements Action {
  type: any = actionTypes.DUPLICATE_OUTPUT_COMPLETE;
}

export class DuplicateOutputErrorAction implements Action {
  type: any = actionTypes.DUPLICATE_OUTPUT_ERROR;

  constructor(public payload: any) { }
}

export class EditOutputAction implements Action {
  type: any = actionTypes.EDIT_OUTPUT;

  constructor(public payload: any) { }
}

export class CreateOutputAction implements Action {
  type: any = actionTypes.CREATE_OUTPUT;

  constructor(public payload: any) { }
}

export class CreateOutputCompleteAction implements Action {
  type: any = actionTypes.CREATE_OUTPUT_COMPLETE;
}

export class CreateOutputErrorAction implements Action {
  type: any = actionTypes.CREATE_OUTPUT_ERROR;

  constructor(public payload: any) { }
}

export class UpdateOutputAction implements Action {
  type: any = actionTypes.UPDATE_OUTPUT;

  constructor(public payload: any) { }
}

export class UpdateOutputCompleteAction implements Action {
  type: any = actionTypes.UPDATE_OUTPUT_COMPLETE;
}

export class UpdateOutputErrorAction implements Action {
  type: any = actionTypes.UPDATE_OUTPUT_ERROR;

  constructor(public payload: any) { }
}

export class ResetOutputFormAction implements Action {
  type: any = actionTypes.RESET_OUTPUT_FORM;
}

export type Actions =
  ListOutputAction |
  ListOutputFailAction |
  ListOutputCompleteAction |
  SelectOutputAction |
  DeselectOutputAction |
  DeleteOutputAction |
  DeleteOutputCompleteAction |
  DeleteOutputErrorAction |
  DuplicateOutputAction |
  DuplicateOutputCompleteAction |
  DuplicateOutputErrorAction |
  EditOutputAction |
  CreateOutputAction |
  CreateOutputCompleteAction |
  CreateOutputErrorAction |
  UpdateOutputAction |
  UpdateOutputCompleteAction |
  UpdateOutputErrorAction |
  DisplayModeAction |
  ResetOutputFormAction;

