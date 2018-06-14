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

export class InitDebugWorkflowAction implements Action {
    readonly type = INIT_DEBUG_WORKFLOW;
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
    ChangeSelectedConsoleTab;
