/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { GlobalParam } from '@app/settings/parameter-group/models/globalParam';
import { Store } from '@ngrx/store';
import * as fromParameters from '../../reducers';
import * as alertParametersActions from '../../actions/alert';
import * as customParametersActions from '../../actions/custom';

@Component({
   selector: 'custom-parameters',
   templateUrl: './custom-parameters.component.html',
   styleUrls: ['./custom-parameters.component.scss']
})
export class CustomParametersComponent implements OnInit {

   public alertMessage = '';
   public showAlert: boolean;

   @Input() customParams: GlobalParam[];
   @Input() customList: any[];
   @Input() customContexts: string[];
   @Input() breadcrumbList: any;
   @Input() creationMode: boolean;

   @Output() navigate = new EventEmitter<any>();
   @Output() saveParam = new EventEmitter<any>();
   @Output() addCustomParam = new EventEmitter<any>();
   @Output() addCustomList = new EventEmitter<any>();
   @Output() saveList = new EventEmitter<any>();
   @Output() saveContext = new EventEmitter<any>();
   @Output() deleteParam = new EventEmitter<any>();
   @Output() goToCustom = new EventEmitter<any>();
   @Output() addContext = new EventEmitter<any>();
   @Output() changeContext = new EventEmitter<any>();
   @Output() search: EventEmitter<{filter?: string, text: string}> = new EventEmitter<{filter?: string, text: string}>();
   @Output() addCustomContext =  new EventEmitter<any>();
   @Output() deleteList = new EventEmitter<any>();
   @Output() deleteContext = new EventEmitter<any>();

   public showConfigContext = false;


   constructor(private _store: Store<fromParameters.State>, private _cd: ChangeDetectorRef) {}

   ngOnInit(): void {
      this._store.select(fromParameters.showAlert).subscribe(alert => {
         this.showAlert = !!alert;
         if (alert) {
            this.alertMessage = alert;
         } else {
            this._cd.markForCheck();
         }
      });
   }

   navigateToList(list) {
      this.navigate.emit(list);
   }

   saveParamContexts(param) {
      this.saveParam.emit(param);
   }

   addParam() {
      if (this.customList) {
         this.addCustomList.emit();
      } else {
         this.addCustomParam.emit();
      }
   }

   saveParamList(list: any) {
      this.saveList.emit(list);
   }

   saveParamContext(list: any) {
      this.saveContext.emit(list);
   }



   deleteCustomParam(param) {
      this.deleteParam.emit(param);
   }

   goCustom() {
      this.goToCustom.emit();
   }

   onChangeContext(context) {
      this.changeContext.emit(context);
   }

   addNewContext(context) {
      this.addContext.emit(context);
   }

   toggleConfigContext() {
      this.showConfigContext = !this.showConfigContext;
      this._store.dispatch(new alertParametersActions.HideAlertAction());
      if (!this.showConfigContext) {
         this._store.dispatch(new customParametersActions.NavigateToListAction({ name: this.breadcrumbList.name  }));
      }
   }

   onAddCustomContext() {
      this.addCustomContext.emit();
   }

   onDeleteList(list) {
      this.deleteList.emit(list);
   }

   onDeleteContext(list) {
      this.deleteContext.emit(list);
   }

}
