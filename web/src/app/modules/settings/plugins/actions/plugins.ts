/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';

export const LIST_DRIVERS = '[Resources] List drivers';
export const LIST_DRIVERS_COMPLETE = '[Resources] List drivers complete';
export const LIST_DRIVERS_ERROR = '[Resources] List drivers error';
export const LIST_PLUGINS = '[Resources] List plugins';
export const LIST_PLUGINS_COMPLETE = '[Resources] List plugins complete';
export const LIST_PLUGINS_ERROR = '[Resources] List plugins error';
export const SELECT_PLUGIN = '[Resources] Select plugin';
export const UNSELECT_PLUGIN = '[Resources] Unselect plugin';
export const SELECT_DRIVER = '[Resources] Select driver';
export const UNSELECT_DRIVER = '[Resources] Unselect driver';
export const UPLOAD_PLUGIN = '[Resources] Upload plugin';
export const UPLOAD_PLUGIN_COMPLETE = '[Resources] Upload plugin complete';
export const UPLOAD_PLUGIN_ERROR = '[Resources] Upload plugin error';
export const DELETE_PLUGIN = '[Resources] Delete plugin';
export const DELETE_PLUGIN_COMPLETE = '[Resources] Delete plugin complete';
export const DELETE_PLUGIN_ERROR = '[Resources] Delete plugin error';
export const UPLOAD_DRIVER = '[Resources] Upload driver';
export const UPLOAD_DRIVER_COMPLETE = '[Resources] Upload driver complete';
export const UPLOAD_DRIVER_ERROR = '[Resources] Upload driver error';
export const DELETE_DRIVER = '[Resources] Delete driver';
export const DELETE_DRIVER_COMPLETE = '[Resources] Delete driver complete';
export const DELETE_DRIVER_ERROR = '[Resources] Delete driver error';
export const DOWNLOAD_DRIVER = '[Resources] Download driver';
export const DOWNLOAD_DRIVER_COMPLETE = '[Resources] Download driver complete';
export const DOWNLOAD_DRIVER_ERROR = '[Resources] Download driver error';
export const DOWNLOAD_PLUGIN = '[Resources] Download plugin';
export const DOWNLOAD_PLUGIN_COMPLETE = '[Resources] Download plugin complete';
export const DOWNLOAD_PLUGIN_ERROR = '[Resources] Download plugin error';
export const CHANGE_ORDER_DRIVERS = '[Resources] Change order drivers';
export const CHANGE_ORDER_PLUGINS = '[Resources] Change order plugins';
export const SELECT_ALL_PLUGINS = '[Resources] Select all plugins';

export class ListDriversAction implements Action {
  readonly type = LIST_DRIVERS;

  constructor() { }
}

export class ListDriversCompleteAction implements Action {
  readonly type = LIST_DRIVERS_COMPLETE;

  constructor(public payload: any) { }
}

export class ListDriversErrorAction implements Action {
  readonly type = LIST_DRIVERS_ERROR;

  constructor(public payload: any) { }
}


export class ListPluginsAction implements Action {
  readonly type = LIST_PLUGINS;

  constructor() { }
}

export class ListPluginsCompleteAction implements Action {
  readonly type = LIST_PLUGINS_COMPLETE;

  constructor(public payload: any) { }
}

export class ListPluginsErrorAction implements Action {
  readonly type = LIST_PLUGINS_ERROR;

  constructor(public payload: any) { }
}

export class SelectPluginAction implements Action {
  readonly type = SELECT_PLUGIN;

  constructor(public payload: any) { }
}

export class UnselectPluginAction implements Action {
  readonly type = UNSELECT_PLUGIN;

  constructor(public payload: any) { }
}

export class SelectDriverAction implements Action {
  readonly type = SELECT_DRIVER;

  constructor(public payload: any) { }
}

export class UnselectDriverAction implements Action {
  readonly type = UNSELECT_DRIVER;

  constructor(public payload: any) { }
}

export class UploadPluginAction implements Action {
  readonly type = UPLOAD_PLUGIN;

  constructor(public payload: any) { }
}


export class UploadPluginCompleteAction implements Action {
  readonly type = UPLOAD_PLUGIN_COMPLETE;

  constructor(public payload: any) { }
}


export class UploadPluginErrorAction implements Action {
  readonly type = UPLOAD_PLUGIN_ERROR;

  constructor(public payload: any) { }
}


export class DeletePluginAction implements Action {
  readonly type = DELETE_PLUGIN;
}


export class DeletePluginCompleteAction implements Action {
  readonly type = DELETE_PLUGIN_COMPLETE;

  constructor(public payload: any) { }
}


export class DeletePluginErrorAction implements Action {
  readonly type = DELETE_PLUGIN_ERROR;

  constructor(public payload: any) { }
}


export class UploadDriverAction implements Action {
  readonly type = UPLOAD_DRIVER;

  constructor(public payload: any) { }
}


export class UploadDriverCompleteAction implements Action {
  readonly type = UPLOAD_DRIVER_COMPLETE;

  constructor(public payload: any) { }
}


export class UploadDriverErrorAction implements Action {
  readonly type = UPLOAD_DRIVER_ERROR;

  constructor(public payload: any) { }
}


export class DeleteDriverAction implements Action {
  readonly type = DELETE_DRIVER;

  constructor(public payload: any) { }
}


export class DeleteDriverCompleteAction implements Action {
  readonly type = DELETE_DRIVER_COMPLETE;

  constructor(public payload: any) { }
}


export class DeleteDriverErrorAction implements Action {
  readonly type = DELETE_DRIVER_ERROR;

  constructor(public payload: any) { }
}


export class DownloadDriverAction implements Action {
  readonly type = DOWNLOAD_DRIVER;

  constructor(public payload: any) { }
}


export class DownloadDriverCompleteAction implements Action {
  readonly type = DOWNLOAD_DRIVER_COMPLETE;

  constructor(public payload: any) { }
}


export class DownloadDriverErrorAction implements Action {
  readonly type = DOWNLOAD_DRIVER_ERROR;

  constructor(public payload: any) { }
}


export class DownloadPluginAction implements Action {
  readonly type = DOWNLOAD_PLUGIN;

  constructor(public payload: any) { }
}


export class DownloadPluginCompleteAction implements Action {
  readonly type = DOWNLOAD_PLUGIN_COMPLETE;

  constructor(public payload: any) { }
}


export class DownloadPluginErrorAction implements Action {
  readonly type = DOWNLOAD_PLUGIN_ERROR;

  constructor(public payload: any) { }
}

export class ChangeOrderDrivers implements Action {
  readonly type = CHANGE_ORDER_DRIVERS;

  constructor(public payload: any) { }
}

export class ChangeOrderPlugins implements Action {
  readonly type = CHANGE_ORDER_PLUGINS;

  constructor(public payload: any) { }
}

export class SelectAllPluginsAction implements Action {
  readonly type = SELECT_ALL_PLUGINS;
  constructor(public payload: any) { }
}

export type Actions = ListDriversAction |
  ListDriversCompleteAction |
  ListDriversErrorAction |
  ListPluginsAction |
  ListPluginsCompleteAction |
  ListPluginsErrorAction |
  SelectDriverAction |
  UnselectDriverAction |
  SelectPluginAction |
  UnselectPluginAction |
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
  DownloadDriverErrorAction |
  ChangeOrderDrivers |
  ChangeOrderPlugins |
  SelectAllPluginsAction;

