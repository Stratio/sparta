/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';

import * as fromParameters from './../../reducers';
import * as customParamsActions from './../../actions/custom';
import { Observable } from 'rxjs';
import { GlobalParam } from '@app/settings/parameter-group/models/globalParam';

@Component({
  selector: 'custom-parameters-container',
  template: `
    <custom-parameters
      [customParams]="customParams$ | async"
      [customList]="customLists$ | async"
      [customContexts]="customContexts$ | async"
      [breadcrumbList]="breadcrumbList$ | async"
      (saveParam)="onSaveParam($event)"
      (navigate)="navigateToList($event)"
      (addCustomParam)="onAddCustomParam()"
      (addCustomList)="onAddCustomList()"
      (saveList)="onSaveList($event)"
      (saveContext)="onSaveContext($event)"
      (deleteParam)="onDeleteParam($event)"
      (goToCustom)="goCustom()"
      (changeContext)="onChangeContext($event)"
      (addContext)="onAddContext($event)"
      (search)="searchCustom($event)"
      [creationMode]="creationMode"
      (addCustomContext)="onAddCustomContext()"
      (deleteList)="onDeleteList($event)"
      (deleteContext)="onDeleteContext($event)"
      >

   </custom-parameters>
  `
})
export class CustomParametersContainer implements OnInit {
   public customParams$: Observable<GlobalParam[]>;
   public customLists$: Observable<GlobalParam[]>;
   public customContexts$: Observable<string[]>;
   public breadcrumbList$: Observable<any>;
   public creationMode = false;

   constructor(private _store: Store<fromParameters.State>, private _cd: ChangeDetectorRef) { }

   ngOnInit(): void {
      this._init();
   }

   private _init() {
      this._store.dispatch(new customParamsActions.ListCustomParamsAction());
      this.customLists$ = this._store.select(fromParameters.getCustomList);
      this.customParams$ = this._store.select(fromParameters.getCustomParams);
      this.customContexts$ = this._store.select(fromParameters.getCustomContexts);
      this.breadcrumbList$ = this._store.select(fromParameters.getSelectedList);
      this._store.select(fromParameters.getCustomIsCreating)
         .subscribe((isCreating: boolean) =>{
            this.creationMode = isCreating;
            this._cd.markForCheck();
         });
   }

   navigateToList(list) {
      this._store.dispatch(new customParamsActions.NavigateToListAction(list));
   }

   onSaveParam(param) {
      this._store.dispatch(new customParamsActions.SaveParam(param));
   }

   onAddCustomParam() {
      this._store.dispatch(new customParamsActions.AddCustomParamsAction());
   }

   onAddCustomList() {
      this._store.dispatch(new customParamsActions.AddCustomListAction());
   }

   onSaveList(list) {
      this._store.dispatch(new customParamsActions.SaveCustomListAction(list));
   }

   onSaveContext(context) {
      this._store.dispatch(new customParamsActions.SaveCustomContextAction(context));
   }

   onDeleteParam(param) {
      this._store.dispatch(new customParamsActions.DeleteCustomAction(param));
   }

   goCustom() {
      this._store.dispatch(new customParamsActions.GoCustomAction());
   }

   onChangeContext(context) {
      this._store.dispatch(new customParamsActions.ChangeContextOptionAction(context));
   }

   onAddContext(context) {
      this._store.dispatch(new customParamsActions.AddContextAction(context.context));
   }

   searchCustom(global) {
      this._store.dispatch(new customParamsActions.SearchCustomAction(global));
   }

   onAddCustomContext() {
      this._store.dispatch(new customParamsActions.AddCustomContextAction());
   }

   onDeleteList(list) {
      this._store.dispatch(new customParamsActions.DeleteListAction(list));
   }

   onDeleteContext(context) {
      this._store.dispatch(new customParamsActions.DeleteContextAction(context));
   }


}
