/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';

import * as fromParameters from './../../reducers';
import * as globalParamsActions from './../../actions/global';
import * as alertParametersActions from './../../actions/alert';

import { GlobalParam } from './../../models/globalParam';

@Component({
  selector: 'global-parameters-container',
  template: `
    <global-parameters
      [globalParams]="globalParams$ | async"
      (addGlobalParam)="onAddGlobalParam()"
      (saveParam)="onSaveParam($event)"
      (deleteParam)="onDeleteParam($event)"
      (search)="searchGlobal($event)"
      [creationMode]="creationMode"
      (onDownloadParams)="downloadParams()"
      (onUploadParams)="uploadParams($event)"
      (emitAlert)="onEmitAlert($event)"
      ></global-parameters>
  `
})
export class GlobalParametersContainer implements OnInit {

    public globalParams$: Observable<GlobalParam[]>;
    public creationMode = false;

    constructor(private _store: Store<fromParameters.State>) { }

    ngOnInit(): void {
        this._init();
    }

    private _init() {
        this._store.dispatch(new globalParamsActions.ListGlobalParamsAction());
        this.initRequest();
        this.globalParams$ = this._store.pipe(select(fromParameters.getGlobalVariables));
        this._store.pipe(select(fromParameters.getIsCreating))
            .subscribe((isCreating: boolean) => this.creationMode = isCreating);
    }

    initRequest() {
        this._store.dispatch(new alertParametersActions.ShowLoadingAction());
    }
    onAddGlobalParam() {
        this._store.dispatch(new globalParamsActions.AddGlobalParamsAction());
    }

    onSaveParam(param) {
        this.initRequest();
        this._store.dispatch(new globalParamsActions.SaveGlobalAction(param));
    }

    onDeleteParam(param) {
        if (!param.creation) {
            this.initRequest();
        }
        this._store.dispatch(new globalParamsActions.DeleteGlobalAction(param));
    }

    searchGlobal(global) {
        this._store.dispatch(new globalParamsActions.SearchGlobalAction(global));
    }

    downloadParams() {
        this._store.dispatch(new globalParamsActions.ExportGlobalParamsAction());
    }

    uploadParams(globals) {
        this._store.dispatch(new globalParamsActions.ImportGlobalParamsAction(globals));
    }

    onEmitAlert(message) {
        this._store.dispatch(new alertParametersActions.ShowAlertAction({ type: 'critical', text: message }));
    }
}
