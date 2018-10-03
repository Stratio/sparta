/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';

export const GET_PARAMS_LIST = '[Wizard] Get params list';
export const GET_PARAMS_LIST_COMPLETE = '[Wizard] Get params list complete';
export const GET_PARAMS_LIST_ERROR = '[Wizard] Get params list error';

export class GetParamsListAction implements Action {
    readonly type = GET_PARAMS_LIST;
}

export class GetParamsListCompleteAction implements Action {
    readonly type = GET_PARAMS_LIST_COMPLETE;
    constructor(public payload: any) { }
}

export class GetParamsListErrorAction implements Action {
    readonly type = GET_PARAMS_LIST_ERROR;
}

export type Actions =
    GetParamsListAction |
    GetParamsListCompleteAction |
    GetParamsListErrorAction;