/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { GlobalParam } from '@app/settings/parameter-group/models/globalParam';

@Component({
   selector: "custom-parameters",
   templateUrl: "./custom-parameters.component.html",
   styleUrls: ["./custom-parameters.component.scss"]
})
export class CustomParametersComponent implements OnInit {
   @Input() customParams: GlobalParam[];
   @Input() customList: any[];
   @Input() customContexts: string[];
   @Input() breadcrumbList: any;
   @Output() navigate = new EventEmitter<any>();
   @Output() saveParam = new EventEmitter<any>();
   @Output() addCustomParam = new EventEmitter<any>();
   @Output() addCustomList = new EventEmitter<any>();
   @Output() saveList = new EventEmitter<any>();
   @Output() deleteParam = new EventEmitter<any>();
   @Output() goToCustom = new EventEmitter<any>();
   @Output() addContext = new EventEmitter<any>();
   @Output() changeContext = new EventEmitter<any>();
   @Output() search: EventEmitter<{filter?: string, text: string}> = new EventEmitter<{filter?: string, text: string}>();


   constructor() {}

   ngOnInit(): void {}

   navigateToList(list) {
      this.navigate.emit(list);
   }

   saveParamContexts(param) {
      this.saveParam.emit(param);
   }

   addParam() {
      if (this.customList.length) {
         this.addCustomList.emit();
      } else {
         this.addCustomParam.emit();
      }
   }

   saveParamList(list: any) {
      this.saveList.emit(list);
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
}
