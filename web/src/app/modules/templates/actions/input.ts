/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';

export const LIST_INPUT = '[Input] List inputs';
export const LIST_INPUT_COMPLETE = '[Input] List inputs complete';
export const LIST_INPUT_FAIL = '[Input] List inputs fail';
export const GET_EDITED_INPUT = '[Input] Get edited input';
export const GET_EDITED_INPUT_COMPLETE = '[Input] Get edited input complete';
export const GET_EDITED_INPUT_ERROR = '[Input] Get edited input error';
export const SELECT_INPUT = '[Input] Select input';
export const DESELECT_INPUT = '[Input] Deselect input';
export const DELETE_INPUT = '[Input] Delete input';
export const DELETE_INPUT_COMPLETE = '[Input] Delete input complete';
export const DELETE_INPUT_ERROR = '[Input] Delete input error';
export const DISPLAY_MODE = '[Input] change display mode';
export const DUPLICATE_INPUT = '[Input] duplicate selected input';
export const DUPLICATE_INPUT_COMPLETE = '[Input] Duplicate input complete';
export const DUPLICATE_INPUT_ERROR = '[Input] duplicate input error';
export const EDIT_INPUT = '[Input] Edit input';
export const CREATE_INPUT = '[Input] Create input';
export const CREATE_INPUT_COMPLETE = '[Input] Create input complete';
export const CREATE_INPUT_ERROR = '[Input] Create input error';
export const UPDATE_INPUT = '[Input] Update input';
export const UPDATE_INPUT_COMPLETE = '[Input] Update input complete';
export const UPDATE_INPUT_ERROR = '[Input] Update input error';
export const RESET_INPUT_FORM = '[Input] Reset input form';
export const VALIDATE_INPUT_NAME = '[Input] Validate input name';
export const CHANGE_ORDER = '[Input] Change order';

export class ListInputAction implements Action {
  readonly type = LIST_INPUT;

  constructor() { }
}
export class ListInputFailAction implements Action {
  readonly type = LIST_INPUT_FAIL;

  constructor(public payload: any) { }
}

export class ListInputCompleteAction implements Action {
  readonly type = LIST_INPUT_COMPLETE;

  constructor(public payload: any) { }
}

export class GetEditedInputAction implements Action {
  readonly type = GET_EDITED_INPUT;
    constructor(public payload: any) { }
}

export class GetEditedInputCompleteAction implements Action {
  readonly type = GET_EDITED_INPUT_COMPLETE;
  constructor(public payload: any) { }
}

export class GetEditedInputErrorAction implements Action {
  readonly type = GET_EDITED_INPUT_ERROR;
    constructor(public payload: any) { }
}

export class SelectInputAction implements Action {
  readonly type = SELECT_INPUT;

  constructor(public payload: any) { }
}

export class DeselectInputAction implements Action {
  readonly type = DESELECT_INPUT;

  constructor(public payload: any) { }
}

export class DeleteInputAction implements Action {
  readonly type = DELETE_INPUT;

  constructor(public payload: any) { }
}

export class DeleteInputCompleteAction implements Action {
  readonly type = DELETE_INPUT_COMPLETE;

  constructor(public payload: any) { }
}

export class DeleteInputErrorAction implements Action {
  readonly type = DELETE_INPUT_ERROR;

  constructor(public payload: any) { }
}

export class DisplayModeAction implements Action {
  readonly type = DISPLAY_MODE;

  constructor() { }
}

export class ValidateInputNameAction implements Action {
  readonly type = VALIDATE_INPUT_NAME;

  constructor(public payload: String) { }
}

export class DuplicateInputAction implements Action {
  readonly type = DUPLICATE_INPUT;

  constructor(public payload: any) { }
}

export class DuplicateInputCompleteAction implements Action {
  readonly type = DUPLICATE_INPUT_COMPLETE;
}

export class DuplicateInputErrorAction implements Action {
  readonly type = DUPLICATE_INPUT_ERROR;

  constructor(public payload: any) { }
}

export class EditInputAction implements Action {
  readonly type = EDIT_INPUT;

  constructor(public payload: any) { }
}

export class CreateInputAction implements Action {
  readonly type = CREATE_INPUT;

  constructor(public payload: any) { }
}

export class CreateInputCompleteAction implements Action {
  readonly type = CREATE_INPUT_COMPLETE;
}

export class CreateInputErrorAction implements Action {
  readonly type = CREATE_INPUT_ERROR;

  constructor(public payload: any) { }
}

export class UpdateInputAction implements Action {
  readonly type = UPDATE_INPUT;

  constructor(public payload: any) { }
}

export class UpdateInputCompleteAction implements Action {
  readonly type = UPDATE_INPUT_COMPLETE;
}

export class UpdateInputErrorAction implements Action {
  readonly type = UPDATE_INPUT_ERROR;

  constructor(public payload: any) { }
}

export class ResetInputFormAction implements Action {
  readonly type = RESET_INPUT_FORM;
}

export class ChangeOrderAction implements Action {
  readonly type = CHANGE_ORDER;
  constructor(public payload: any) { }
}

export type Actions =
  ListInputAction |
  ListInputFailAction |
  ListInputCompleteAction |
  GetEditedInputAction |
  GetEditedInputCompleteAction |
  GetEditedInputErrorAction |
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
  DisplayModeAction |
  ResetInputFormAction |
  ChangeOrderAction;
