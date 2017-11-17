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

export const LIST_TRANSFORMATION = '[Transformation] List inputs';
export const LIST_TRANSFORMATION_COMPLETE = '[Transformation] List inputs complete';
export const LIST_TRANSFORMATION_FAIL = '[Transformation] List inputs fail';
export const SELECT_TRANSFORMATION = '[Transformation] Select input';
export const DESELECT_TRANSFORMATION = '[Transformation] Deselect input';
export const DELETE_TRANSFORMATION = '[Transformation] Delete input';
export const DELETE_TRANSFORMATION_COMPLETE = '[Transformation] Delete input complete';
export const DELETE_TRANSFORMATION_ERROR = '[Transformation] Delete input error';
export const DISPLAY_MODE = '[Transformation] change display mode';
export const DUPLICATE_TRANSFORMATION = '[Transformation] duplicate selected input';
export const DUPLICATE_TRANSFORMATION_COMPLETE = '[Transformation] Duplicate input complete';
export const DUPLICATE_TRANSFORMATION_ERROR = '[Transformation] duplicate input error';
export const EDIT_TRANSFORMATION = '[Transformation] Edit input';
export const CREATE_TRANSFORMATION = '[Transformation] Create input';
export const CREATE_TRANSFORMATION_COMPLETE = '[Transformation] Create input complete';
export const CREATE_TRANSFORMATION_ERROR = '[Transformation] Create input error';
export const UPDATE_TRANSFORMATION = '[Transformation] Update input';
export const UPDATE_TRANSFORMATION_COMPLETE = '[Transformation] Update input complete';
export const UPDATE_TRANSFORMATION_ERROR = '[Transformation] Update input error';
export const RESET_TRANSFORMATION_FORM = '[Transformation] Reset input form';
export const VALIDATE_TRANSFORMATION_NAME = '[Transformation] Validate input name';
export const CHANGE_ORDER = '[Transformation] Change order';

export class ListTransformationAction implements Action {
  readonly type = LIST_TRANSFORMATION;

  constructor() { }
}
export class ListTransformationFailAction implements Action {
  readonly type = LIST_TRANSFORMATION_FAIL;

  constructor(public payload: any) { }
}

export class ListTransformationCompleteAction implements Action {
  readonly type = LIST_TRANSFORMATION_COMPLETE;

  constructor(public payload: any) { }
}

export class SelectTransformationAction implements Action {
  readonly type = SELECT_TRANSFORMATION;

  constructor(public payload: any) { }
}

export class DeselectTransformationAction implements Action {
  readonly type = DESELECT_TRANSFORMATION;

  constructor(public payload: any) { }
}

export class DeleteTransformationAction implements Action {
  readonly type = DELETE_TRANSFORMATION;

  constructor(public payload: any) { }
}

export class DeleteTransformationCompleteAction implements Action {
  readonly type = DELETE_TRANSFORMATION_COMPLETE;

  constructor(public payload: any) { }
}

export class DeleteTransformationErrorAction implements Action {
  readonly type = DELETE_TRANSFORMATION_ERROR;

  constructor(public payload: any) { }
}

export class DisplayModeAction implements Action {
  readonly type = DISPLAY_MODE;

  constructor() { }
}

export class ValidateTransformationNameAction implements Action {
  readonly type = VALIDATE_TRANSFORMATION_NAME;

  constructor(public payload: String) { }
}

export class DuplicateTransformationAction implements Action {
  readonly type = DUPLICATE_TRANSFORMATION;

  constructor(public payload: any) { }
}

export class DuplicateTransformationCompleteAction implements Action {
  readonly type = DUPLICATE_TRANSFORMATION_COMPLETE;
}

export class DuplicateTransformationErrorAction implements Action {
  readonly type = DUPLICATE_TRANSFORMATION_ERROR;

  constructor(public payload: any) { }
}

export class EditTransformationAction implements Action {
  readonly type = EDIT_TRANSFORMATION;

  constructor(public payload: any) { }
}

export class CreateTransformationAction implements Action {
  readonly type = CREATE_TRANSFORMATION;

  constructor(public payload: any) { }
}

export class CreateTransformationCompleteAction implements Action {
  readonly type = CREATE_TRANSFORMATION_COMPLETE;
}

export class CreateTransformationErrorAction implements Action {
  readonly type = CREATE_TRANSFORMATION_ERROR;

  constructor(public payload: any) { }
}

export class UpdateTransformationAction implements Action {
  readonly type = UPDATE_TRANSFORMATION;

  constructor(public payload: any) { }
}

export class UpdateTransformationCompleteAction implements Action {
  readonly type = UPDATE_TRANSFORMATION_COMPLETE;
}

export class UpdateTransformationErrorAction implements Action {
  readonly type = UPDATE_TRANSFORMATION_ERROR;

  constructor(public payload: any) { }
}

export class ResetTransformationFormAction implements Action {
  readonly type = RESET_TRANSFORMATION_FORM;
}

export class ChangeOrderAction implements Action {
  readonly type = CHANGE_ORDER;
  constructor(public payload: any) { }
}

export type Actions =
  ListTransformationAction |
  ListTransformationFailAction |
  ListTransformationCompleteAction |
  SelectTransformationAction |
  DeselectTransformationAction |
  DeleteTransformationAction |
  DeleteTransformationCompleteAction |
  DeleteTransformationErrorAction |
  DuplicateTransformationAction |
  DuplicateTransformationCompleteAction |
  DuplicateTransformationErrorAction |
  EditTransformationAction |
  CreateTransformationAction |
  CreateTransformationCompleteAction |
  CreateTransformationErrorAction |
  UpdateTransformationAction |
  UpdateTransformationCompleteAction |
  UpdateTransformationErrorAction |
  DisplayModeAction |
  ResetTransformationFormAction |
  ChangeOrderAction;
