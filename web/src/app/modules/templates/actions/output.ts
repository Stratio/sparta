/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';
import { type } from '@utils';

export const LIST_OUTPUT = '[Output] List outputs';
export const LIST_OUTPUT_COMPLETE = '[Output] List outputs complete';
export const LIST_OUTPUT_FAIL = '[Output] List outputs fail';
export const GET_EDITED_OUTPUT = '[Output] Get edited output';
export const GET_EDITED_OUTPUT_COMPLETE = '[Output] Get edited output complete';
export const GET_EDITED_OUTPUT_ERROR = '[Output] Get edited output error';
export const SELECT_OUTPUT = '[Output] Select output';
export const DESELECT_OUTPUT = '[Output] Deselect output';
export const DELETE_OUTPUT = '[Output] Delete output';
export const DELETE_OUTPUT_COMPLETE = '[Output] Delete output complete';
export const DELETE_OUTPUT_ERROR = '[Output] Delete output error';
export const DISPLAY_MODE = '[Output] change display mode';
export const DUPLICATE_OUTPUT = '[Output] duplicate selected output';
export const DUPLICATE_OUTPUT_COMPLETE = '[Output] Duplicate output complete';
export const DUPLICATE_OUTPUT_ERROR = '[Output] duplicate output error';
export const EDIT_OUTPUT = '[Output] Edit output';
export const CREATE_OUTPUT = '[Output] Create output';
export const CREATE_OUTPUT_COMPLETE = '[Output] Create output complete';
export const CREATE_OUTPUT_ERROR = '[Output] Create output error';
export const UPDATE_OUTPUT = '[Output] Update output';
export const UPDATE_OUTPUT_COMPLETE = '[Output] Update output complete';
export const UPDATE_OUTPUT_ERROR = '[Output] Update output error';
export const RESET_OUTPUT_FORM = '[Output] Reset output form';
export const VALIDATE_OUTPUT_NAME = '[Output] Validate output name';
export const CHANGE_ORDER = '[Output] Change order';

export class ListOutputAction implements Action {
  readonly type = LIST_OUTPUT;

  constructor() { }
}
export class ListOutputFailAction implements Action {
  readonly type = LIST_OUTPUT_FAIL;

  constructor(public payload: any) { }
}

export class ListOutputCompleteAction implements Action {
  readonly type = LIST_OUTPUT_COMPLETE;

  constructor(public payload: any) { }
}

export class GetEditedOutputAction implements Action {
  readonly type = GET_EDITED_OUTPUT;
    constructor(public payload: any) { }
}

export class GetEditedOutputCompleteAction implements Action {
  readonly type = GET_EDITED_OUTPUT_COMPLETE;
  constructor(public payload: any) { }
}

export class GetEditedOutputErrorAction implements Action {
  readonly type = GET_EDITED_OUTPUT_ERROR;
    constructor(public payload: any) { }
}

export class SelectOutputAction implements Action {
  readonly type = SELECT_OUTPUT;

  constructor(public payload: any) { }
}

export class DeselectOutputAction implements Action {
  readonly type = DESELECT_OUTPUT;

  constructor(public payload: any) { }
}

export class DeleteOutputAction implements Action {
  readonly type = DELETE_OUTPUT;

  constructor(public payload: any) { }
}

export class DeleteOutputCompleteAction implements Action {
  readonly type = DELETE_OUTPUT_COMPLETE;

  constructor(public payload: any) { }
}

export class DeleteOutputErrorAction implements Action {
  readonly type = DELETE_OUTPUT_ERROR;

  constructor(public payload: any) { }
}

export class DisplayModeAction implements Action {
  readonly type = DISPLAY_MODE;
}

export class ValidateOutputNameAction implements Action {
  readonly type = VALIDATE_OUTPUT_NAME;

  constructor(public payload: String) { }
}

export class DuplicateOutputAction implements Action {
  readonly type = DUPLICATE_OUTPUT;

  constructor(public payload: any) { }
}

export class DuplicateOutputCompleteAction implements Action {
  readonly type = DUPLICATE_OUTPUT_COMPLETE;
}

export class DuplicateOutputErrorAction implements Action {
  readonly type = DUPLICATE_OUTPUT_ERROR;

  constructor(public payload: any) { }
}

export class EditOutputAction implements Action {
  readonly type = EDIT_OUTPUT;

  constructor(public payload: any) { }
}

export class CreateOutputAction implements Action {
  readonly type = CREATE_OUTPUT;

  constructor(public payload: any) { }
}

export class CreateOutputCompleteAction implements Action {
  readonly type = CREATE_OUTPUT_COMPLETE;
}

export class CreateOutputErrorAction implements Action {
  readonly type = CREATE_OUTPUT_ERROR;

  constructor(public payload: any) { }
}

export class UpdateOutputAction implements Action {
  readonly type = UPDATE_OUTPUT;

  constructor(public payload: any) { }
}

export class UpdateOutputCompleteAction implements Action {
  readonly type = UPDATE_OUTPUT_COMPLETE;
}

export class UpdateOutputErrorAction implements Action {
  readonly type = UPDATE_OUTPUT_ERROR;

  constructor(public payload: any) { }
}

export class ResetOutputFormAction implements Action {
  readonly type = RESET_OUTPUT_FORM;
}

export class ChangeOrderAction implements Action {
  readonly type = CHANGE_ORDER;
  constructor(public payload: any) { }
}


export type Actions =
  ListOutputAction |
  ListOutputFailAction |
  ListOutputCompleteAction |
  GetEditedOutputAction |
  GetEditedOutputCompleteAction |
  GetEditedOutputErrorAction |
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
  ResetOutputFormAction |
  ChangeOrderAction;

