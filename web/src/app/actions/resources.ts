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
  LIST_DRIVERS: type('[Resources] List drivers'),
  LIST_DRIVERS_COMPLETE: type('[Resources] List drivers complete'),
  LIST_DRIVERS_ERROR: type('[Resources] List drivers error'),
  LIST_PLUGINS: type('[Resources] List plugins'),
  LIST_PLUGINS_COMPLETE: type('[Resources] List plugins complete'),
  LIST_PLUGINS_ERROR: type('[Resources] List plugins error'),
  UPLOAD_PLUGIN: type('[Resources] Upload plugin'),
  UPLOAD_PLUGIN_COMPLETE: type('[Resources] Upload plugin complete'),
  UPLOAD_PLUGIN_ERROR: type('[Resources] Upload plugin error'),
  DELETE_PLUGIN: type('[Resources] Delete plugin'),
  DELETE_PLUGIN_COMPLETE: type('[Resources] Delete plugin complete'),
  DELETE_PLUGIN_ERROR: type('[Resources] Delete plugin error'),
  UPLOAD_DRIVER: type('[Resources] Upload driver'),
  UPLOAD_DRIVER_COMPLETE: type('[Resources] Upload driver complete'),
  UPLOAD_DRIVER_ERROR: type('[Resources] Upload driver error'),
  DELETE_DRIVER: type('[Resources] Delete driver'),
  DELETE_DRIVER_COMPLETE: type('[Resources] Delete driver complete'),
  DELETE_DRIVER_ERROR: type('[Resources] Delete driver error'),
  DOWNLOAD_DRIVER: type('[Resources] Download driver'),
  DOWNLOAD_DRIVER_COMPLETE: type('[Resources] Download driver complete'),
  DOWNLOAD_DRIVER_ERROR: type('[Resources] Download driver error'),
  DOWNLOAD_PLUGIN: type('[Resources] Download plugin'),
  DOWNLOAD_PLUGIN_COMPLETE: type('[Resources] Download plugin complete'),
  DOWNLOAD_PLUGIN_ERROR: type('[Resources] Download plugin error')
};

export class ListDriversAction implements Action {
  type: any = actionTypes.LIST_DRIVERS;

  constructor() { }
}

export class ListDriversCompleteAction implements Action {
  type: any = actionTypes.LIST_DRIVERS_COMPLETE;

  constructor(public payload: any) { }
}

export class ListDriversErrorAction implements Action {
  type: any = actionTypes.LIST_DRIVERS_ERROR;

  constructor(public payload: any) { }
}


export class ListPluginsAction implements Action {
  type: any = actionTypes.LIST_PLUGINS;

  constructor() { }
}

export class ListPluginsCompleteAction implements Action {
  type: any = actionTypes.LIST_PLUGINS_COMPLETE;

  constructor(public payload: any) { }
}

export class ListPluginsErrorAction implements Action {
  type: any = actionTypes.LIST_PLUGINS_ERROR;

  constructor(public payload: any) { }
}

export class UploadPluginAction implements Action {
  type: any = actionTypes.UPLOAD_PLUGIN;

  constructor(public payload: any) { }
}


export class UploadPluginCompleteAction implements Action {
  type: any = actionTypes.UPLOAD_PLUGIN_COMPLETE;

  constructor(public payload: any) { }
}


export class UploadPluginErrorAction implements Action {
  type: any = actionTypes.UPLOAD_PLUGIN_ERROR;

  constructor(public payload: any) { }
}


export class DeletePluginAction implements Action {
  type: any = actionTypes.DELETE_PLUGIN;

  constructor(public payload: any) { }
}


export class DeletePluginCompleteAction implements Action {
  type: any = actionTypes.DELETE_PLUGIN_COMPLETE;

  constructor(public payload: any) { }
}


export class DeletePluginErrorAction implements Action {
  type: any = actionTypes.DELETE_PLUGIN_ERROR;

  constructor(public payload: any) { }
}


export class UploadDriverAction implements Action {
  type: any = actionTypes.UPLOAD_DRIVER;

  constructor(public payload: any) { }
}


export class UploadDriverCompleteAction implements Action {
  type: any = actionTypes.UPLOAD_DRIVER_COMPLETE;

  constructor(public payload: any) { }
}


export class UploadDriverErrorAction implements Action {
  type: any = actionTypes.UPLOAD_DRIVER_ERROR;

  constructor(public payload: any) { }
}


export class DeleteDriverAction implements Action {
  type: any = actionTypes.DELETE_DRIVER;

  constructor(public payload: any) { }
}


export class DeleteDriverCompleteAction implements Action {
  type: any = actionTypes.DELETE_DRIVER_COMPLETE;

  constructor(public payload: any) { }
}


export class DeleteDriverErrorAction implements Action {
  type: any = actionTypes.DELETE_DRIVER_ERROR;

  constructor(public payload: any) { }
}


export class DownloadDriverAction implements Action {
  type: any = actionTypes.DOWNLOAD_DRIVER;

  constructor(public payload: any) { }
}


export class DownloadDriverCompleteAction implements Action {
  type: any = actionTypes.DOWNLOAD_DRIVER_COMPLETE;

  constructor(public payload: any) { }
}


export class DownloadDriverErrorAction implements Action {
  type: any = actionTypes.DOWNLOAD_DRIVER_ERROR;

  constructor(public payload: any) { }
}


export class DownloadPluginAction implements Action {
  type: any = actionTypes.DOWNLOAD_PLUGIN;

  constructor(public payload: any) { }
}


export class DownloadPluginCompleteAction implements Action {
  type: any = actionTypes.DOWNLOAD_PLUGIN_COMPLETE;

  constructor(public payload: any) { }
}


export class DownloadPluginErrorAction implements Action {
  type: any = actionTypes.DOWNLOAD_PLUGIN_ERROR;

  constructor(public payload: any) { }
}

export type Actions = ListDriversAction |
  ListDriversCompleteAction |
  ListDriversErrorAction |
  ListPluginsAction |
  ListPluginsCompleteAction |
  ListPluginsErrorAction |
  UploadPluginAction |
  UploadPluginCompleteAction |
  UploadPluginErrorAction |
  DeletePluginAction |
  DeletePluginCompleteAction |
  DeletePluginErrorAction |
  UploadDriverAction |
  UploadDriverCompleteAction |
  UploadDriverErrorAction |
  DeleteDriverAction |
  DeleteDriverCompleteAction |
  DeleteDriverErrorAction |
  DownloadPluginAction |
  DownloadPluginCompleteAction |
  DownloadPluginErrorAction |
  DownloadDriverAction |
  DownloadDriverCompleteAction |
  DownloadDriverErrorAction;

