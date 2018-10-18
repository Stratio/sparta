/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, Input, OnInit, EventEmitter, Output } from '@angular/core';
import { GlobalParam } from './../../models/globalParam';

@Component({
  selector: 'global-parameters',
  templateUrl: './global-parameters.component.html',
  styleUrls: ['./global-parameters.component.scss']
})
export class GlobalParametersComponent implements OnInit {

   @Input() globalParams: GlobalParam[];
   @Input() creationMode: boolean;


   @Output() addGlobalParam =  new EventEmitter<any>();
   @Output() saveParam = new EventEmitter<any>();
   @Output() deleteParam = new EventEmitter<any>();
   @Output() search: EventEmitter<{filter?: string, text: string}> = new EventEmitter<{filter?: string, text: string}>();

   constructor() { }

   ngOnInit(): void { }

   addParam() {
      this.addGlobalParam.emit();
   }

   saveGlobalParam(param) {
      this.saveParam.emit(param);
   }

   deleteGlobalParam(param) {
      this.deleteParam.emit(param);
   }
}
