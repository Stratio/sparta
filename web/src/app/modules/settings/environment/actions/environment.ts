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
  FilterEnvironmentAction;
