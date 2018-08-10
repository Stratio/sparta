/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';

export const LIST_ENVIRONMENT = '[Environment] List environment';
export const LIST_ENVIRONMENT_COMPLETE = '[Environment] List environment complete';
export const LIST_ENVIRONMENT_ERROR = '[Environment] List environment error';
export const SAVE_ENVIRONMENT = '[Environment] Save environment';
export const SAVE_ENVIRONMENT_COMPLETE = '[Environment] Save environment complete';
export const SAVE_ENVIRONMENT_ERROR = '[Environment] Save environment error';
export const EXPORT_ENVIRONMENT = '[Environment] Export environment';
export const EXPORT_ENVIRONMENT_COMPLETE = '[Environment] Export environment complete';
export const EXPORT_ENVIRONMENT_ERROR = '[Environment] Export environment error';
export const IMPORT_ENVIRONMENT = '[Environment] Import environment';
export const IMPORT_ENVIRONMENT_COMPLETE = '[Environment] Import environment complete';
export const IMPORT_ENVIRONMENT_ERROR = '[Environment] Import environment error';
export const FILTER_ENVIRONMENT = '[Environment] Filter environment';
export const UPDATE_ENVIRONMENT_VALUE = '[Environment] Update environment value';
export const INVALID_FILE_ERROR = '[Environment] Invalid environment file error';

export class UpdateEnvironmentValueAction implements Action {
  readonly type = UPDATE_ENVIRONMENT_VALUE;
  constructor(public payload: any) { }
}

export class ListEnvironmentAction implements Action {
  readonly type = LIST_ENVIRONMENT;

  constructor() { }
}
export class ListEnvironmentErrorAction implements Action {
  readonly type = LIST_ENVIRONMENT_ERROR;

  constructor(public payload: any) { }
}

export class ListEnvironmentCompleteAction implements Action {
  readonly type = LIST_ENVIRONMENT_COMPLETE;

  constructor(public payload: any) { }
}

export class SaveEnvironmentAction implements Action {
  readonly type = SAVE_ENVIRONMENT;

  constructor(public payload: any) { }
}

export class SaveEnvironmentCompleteAction implements Action {
  readonly type = SAVE_ENVIRONMENT_COMPLETE;

  constructor(public payload: any) { }
}

export class SaveEnvironmentErrorAction implements Action {
  readonly type = SAVE_ENVIRONMENT_ERROR;

  constructor(public payload: any) { }
}

export class ExportEnvironmentAction implements Action {
  readonly type = EXPORT_ENVIRONMENT;
  constructor() {}
}

export class ExportEnvironmentCompleteAction implements Action {
  readonly type = EXPORT_ENVIRONMENT_COMPLETE;
  constructor() {}
}

export class ExportEnvironmentErrorAction implements Action {
  readonly type = EXPORT_ENVIRONMENT_ERROR;
  constructor() {}
}

export class ImportEnvironmentAction implements Action {
  readonly type = IMPORT_ENVIRONMENT;
  constructor(public payload: any) { }
}

export class ImportEnvironmentCompleteAction implements Action {
  readonly type = IMPORT_ENVIRONMENT_COMPLETE;
  constructor() {}
}

export class ImportEnvironmentErrorAction implements Action {
  readonly type = IMPORT_ENVIRONMENT_ERROR;
  constructor() {}
}

export class FilterEnvironmentAction implements Action {
  readonly type = FILTER_ENVIRONMENT;
  constructor(public payload: any) { }
}

export class InvalidEnvironmentFileErrorAction implements Action {
  readonly type = INVALID_FILE_ERROR;
}

export type Actions =
  ListEnvironmentAction |
  ListEnvironmentErrorAction |
  ListEnvironmentCompleteAction |
  SaveEnvironmentAction |
  SaveEnvironmentCompleteAction |
  SaveEnvironmentErrorAction |
  ImportEnvironmentAction |
  ImportEnvironmentCompleteAction |
  ImportEnvironmentErrorAction |
  ExportEnvironmentAction |
  ExportEnvironmentCompleteAction |
  ExportEnvironmentErrorAction |
  FilterEnvironmentAction |
  InvalidEnvironmentFileErrorAction;
