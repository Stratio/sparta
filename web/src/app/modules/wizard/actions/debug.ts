/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';

export const INIT_DEBUG_WORKFLOW = '[Wizard] Init debug workflow';
export const INIT_DEBUG_WORKFLOW_COMPLETE = '[Wizard] Init debug workflow complete';
export const INIT_DEBUG_WORKFLOW_ERROR = '[Wizard] Init debug workflow error';
export const GET_DEBUG_RESULT = '[Wizard] Get debug result';
export const GET_DEBUG_RESULT_COMPLETE = '[Wizard] Get debug result complete';
export const GET_DEBUG_RESULT_ERROR = '[Wizard] Get debug result error';
export const CANCEL_DEBUG_POLLING = '[Wizard] Cancel debug polling';
export const SHOW_DEBUG_CONSOLE = '[Wizard] Show debug console';
export const HIDE_DEBUG_CONSOLE = '[Wizard] Hide debug console';
export const CHANGE_SELECTED_CONSOLE_TAB = '[Wizard] Change selected console tab';
export const UPLOAD_DEBUG_FILE = '[Wizard] Upload debug file';
export const UPLOAD_DEBUG_FILE_COMPLETE = '[Wizard] Upload debug file complete';
export const UPLOAD_DEBUG_FILE_ERROR = '[Wizard] Upload debug file error';
export const DELETE_DEBUG_FILE = '[Wizard] Delete debug file';
export const DELETE_DEBUG_FILE_COMPLETE = '[Wizard] Delete debug file complete';
export const DELETE_DEBUG_FILE_ERROR = '[Wizard] Delete debug file error';
export const DOWNLOAD_DEBUG_FILE = '[Wizard] Download debug file';
export const DOWNLOAD_DEBUG_FILE_COMPLETE = '[Wizard] Download debug file complete';
export const DOWNLOAD_DEBUG_FILE_ERROR = '[Wizard] Download debug file error';
export const SHOW_ENTITY_DEBUG_SCHEMA = '[Wizard] Show entity debug schema';

export const SHOW_DEBUG_CONFIG = '[Wizard] Show debug config';
export const HIDE_DEBUG_CONFIG = '[Wizard] Hide debug config';

export const CONFIG_ADVANCED_EXECUTION = '[Wizard] Config advanced execution';
export const CONFIG_ADVANCED_EXECUTION_COMPLETE = '[Wizard] Config advanced execution complete';
export const CONFIG_ADVANCED_EXECUTION_ERROR = '[Wizard] Config advanced execution error';
export const CANCEL_ADVANCED_EXECUTION = '[Wizard] Cancel advanced execution';

export class CancelAdvancedExecutionAction implements Action {
    readonly type = CANCEL_ADVANCED_EXECUTION;
}

export class ConfigAdvancedExecutionAction implements Action {
    readonly type = CONFIG_ADVANCED_EXECUTION;
}

export class ConfigAdvancedExecutionCompleteAction implements Action {
    readonly type = CONFIG_ADVANCED_EXECUTION_COMPLETE;
    constructor(public config: any) { }
}

export class ConfigAdvancedExecutionErrorAction implements Action {
    readonly type = CONFIG_ADVANCED_EXECUTION_ERROR;
}

export class InitDebugWorkflowAction implements Action {
    readonly type = INIT_DEBUG_WORKFLOW;
    constructor(public config?: any) { }
}

export class InitDebugWorkflowCompleteAction implements Action {
    readonly type = INIT_DEBUG_WORKFLOW_COMPLETE;
    constructor(public payload: any) { }
}

export class InitDebugWorkflowErrorAction implements Action {
    readonly type = INIT_DEBUG_WORKFLOW_ERROR;
}

export class GetDebugResultAction implements Action {
    readonly type = GET_DEBUG_RESULT;
    constructor(public payload: any) { }
}

export class GetDebugResultCompleteAction implements Action {
    readonly type = GET_DEBUG_RESULT_COMPLETE;
    constructor(public payload: any) { }
}

export class GetDebugResultErrorAction implements Action {
    readonly type = GET_DEBUG_RESULT_ERROR;
}

export class CancelDebugPollingAction implements Action {
    readonly type = CANCEL_DEBUG_POLLING;
}

export class ShowDebugConsoleAction implements Action {
    readonly type = SHOW_DEBUG_CONSOLE;
    constructor(public payload: any) { }
}

export class HideDebugConsoleAction implements Action {
    readonly type = HIDE_DEBUG_CONSOLE;
}

export class ChangeSelectedConsoleTab implements Action {
    readonly type = CHANGE_SELECTED_CONSOLE_TAB;
    constructor(public payload: any) { }
}

export class UploadDebugFileAction implements Action {
    readonly type = UPLOAD_DEBUG_FILE;
    constructor(public payload: any) { }
}

export class UploadDebugFileCompleteAction implements Action {
    readonly type = UPLOAD_DEBUG_FILE_COMPLETE;
    constructor(public payload: string) { }
}

export class UploadDebugFileErrorAction implements Action {
    readonly type = UPLOAD_DEBUG_FILE_ERROR;
}

export class DownloadDebugFileAction implements Action {
    readonly type = DOWNLOAD_DEBUG_FILE;
    constructor(public fileName: string) { }
}

export class DownloadDebugFileCompleteAction implements Action {
    readonly type = DOWNLOAD_DEBUG_FILE_COMPLETE;
}

export class DownloadDebugFileErrorAction implements Action {
    readonly type = DOWNLOAD_DEBUG_FILE_ERROR;
}

export class DeleteDebugFileAction implements Action {
    readonly type = DELETE_DEBUG_FILE;
    constructor(public fileName: string) { }
}

export class DeleteDebugFileCompleteAction implements Action {
    readonly type = DELETE_DEBUG_FILE_COMPLETE;
}

export class DeleteDebugFileErrorAction implements Action {
    readonly type = DELETE_DEBUG_FILE_ERROR;
}

export class ShowEntityDebugSchema implements Action {
    readonly type = SHOW_ENTITY_DEBUG_SCHEMA;
    constructor(public entityName: string) { }
}

export class ShowDebugConfigAction implements Action {
    readonly type = SHOW_DEBUG_CONFIG;
}

export class HideDebugConfigAction implements Action {
    readonly type = HIDE_DEBUG_CONFIG;
}

export type Actions =
    InitDebugWorkflowAction |
    InitDebugWorkflowCompleteAction |
    InitDebugWorkflowErrorAction |
    GetDebugResultAction |
    GetDebugResultErrorAction |
    GetDebugResultCompleteAction |
    CancelDebugPollingAction |
    ShowDebugConsoleAction |
    HideDebugConsoleAction |
    ChangeSelectedConsoleTab |
    UploadDebugFileAction |
    UploadDebugFileCompleteAction |
    UploadDebugFileErrorAction |
    DownloadDebugFileAction |
    DownloadDebugFileCompleteAction |
    DownloadDebugFileErrorAction |
    DeleteDebugFileAction |
    DeleteDebugFileCompleteAction |
    DeleteDebugFileErrorAction |
    ShowEntityDebugSchema |
    ShowDebugConfigAction |
    HideDebugConfigAction |
    ConfigAdvancedExecutionAction |
    ConfigAdvancedExecutionCompleteAction |
    ConfigAdvancedExecutionErrorAction |
    CancelAdvancedExecutionAction;
