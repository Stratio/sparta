/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { GlobalParam } from '@app/settings/parameter-group/models/globalParam';

@Component({
  selector: 'environment-parameters',
  templateUrl: './environment-parameters.component.html',
  styleUrls: ['./environment-parameters.component.scss']
})
export class EnvironmentParametersComponent implements OnInit {

   public showConfigContext = false;

   @Input() environmentParams: any;
   @Input() environmentContexts: string[];
   @Input() configContexts: string[];
   @Input() creationMode: boolean;

   @Output() addContext = new EventEmitter<any>();
   @Output() saveParam = new EventEmitter<any>();
   @Output() updateParam = new EventEmitter<any>();
   @Output() addEnvironmentParam =  new EventEmitter<any>();
   @Output() addEnvironmentContext =  new EventEmitter<any>();
   @Output() saveEnvironmentContext =  new EventEmitter<any>();
   @Output() deleteParam = new EventEmitter<any>();
   @Output() changeContext = new EventEmitter<any>();
   @Output() search: EventEmitter<{filter?: string, text: string}> = new EventEmitter<{filter?: string, text: string}>();

  constructor() { }

   ngOnInit(): void { }

   addNewContext(context) {
      this.addContext.emit(context);
   }

   saveParamContexts(param) {
      this.saveParam.emit(param);
   }

   addParam() {
      this.addEnvironmentParam.emit();
   }

   deleteEnvironmentParam(param) {
      this.deleteParam.emit(param);
   }

   onChangeContext(context) {
      this.changeContext.emit(context);
   }

   toggleConfigContext() {
      this.showConfigContext = !this.showConfigContext;
   }

   saveContextList(context) {
      this.saveEnvironmentContext.emit(context);
   }

   onAddEnvironmentContext() {
      this.addEnvironmentContext.emit();
   }
}
