/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';

import * as fromParameters from './../../reducers';
import * as environmentParamsActions from './../../actions/environment';
import { GlobalParam } from '@app/settings/parameter-group/models/globalParam';

@Component({
  selector: 'environment-parameters-container',
  template: `
    <environment-parameters
      [environmentParams]="environmentParams$ | async"
      [environmentContexts]="environmentContexts$ | async"
      (addContext)="onAddContext($event)"
      (saveParam)="onSaveParam($event)"
      (addEnvironmentParam)="onAddEnvironmentParam()"
      (addEnvironmentContext)="onAddEnvironmentContext()"
      (deleteParam)="onDeleteParam($event)"
      (changeContext)="onChangeContext($event)"
      (search)="searchEnvironment($event)"
      (saveEnvironmentContext)="onSaveEnvironmentContext($event)"
       ></environment-parameters>
  `
})
export class EnvironmentParametersContainer implements OnInit {
   public environmentParams$: Observable<GlobalParam[]>;
   public environmentContexts$: Observable<string[]>;
   public configContexts$: Observable<any[]>;

   public list$: Observable<any>;


   constructor(private _store: Store<fromParameters.State>) { }

   ngOnInit(): void {
      this._init();
   }

   private _init() {
      this._store.dispatch(new environmentParamsActions.ListEnvironmentParamsAction());
      this.environmentParams$ = this._store.select(fromParameters.getEnvironmentVariables);
      this.environmentContexts$ = this._store.select(fromParameters.getEnvironmentContexts);

      this.list$ = this._store.select(fromParameters.getListId);
   }

   onAddContext(context) {
      this._store.dispatch(new environmentParamsActions.AddContextAction(context.context));
   }
   onSaveParam(param) {
      this._store.dispatch(new environmentParamsActions.SaveParam(param));
   }

   onAddEnvironmentParam() {
      this._store.dispatch(new environmentParamsActions.AddEnvironmentParamsAction());
   }

   onDeleteParam(param) {
      this._store.dispatch(new environmentParamsActions.DeleteEnviromentAction(param));
   }

   onChangeContext(context) {
      this._store.dispatch(new environmentParamsActions.ChangeContextOptionAction(context));
   }

   searchEnvironment(global) {
      this._store.dispatch(new environmentParamsActions.SearchEnvironmentAction(global));
   }

   onAddEnvironmentContext() {
      this._store.dispatch(new environmentParamsActions.AddEnvironmentContextAction());
   }

   onSaveEnvironmentContext(context) {
      const { list, value: { name } } = context;
      if (list.name !== name) {
         this._store.dispatch(new environmentParamsActions.SaveEnvironmentContext({ ...list, name }));
      }
   }

}
