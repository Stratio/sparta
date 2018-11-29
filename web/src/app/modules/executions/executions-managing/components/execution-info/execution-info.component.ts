/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
import { StTableHeader, StHorizontalTab } from '@stratio/egeo';
import { Store } from '@ngrx/store';

import * as fromRoot from 'reducers';

@Component({
   selector: 'execution-info',
   templateUrl: './execution-info.component.html',
   styleUrls: ['./execution-info.component.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExecutionInfoComponent {

   @Input() executionInfo: any;
   @Output() closeExecutionInfo = new EventEmitter<void>();

   public sortOrderConfig = false;
   public orderByConfig = 'key';

   public orderByArguments = 'key';
   public sortOrderArguments = false;
   public options: StHorizontalTab[] = [{
      id: 'spark',
      text: 'Spark Configurations'
   },
   {
      id: 'submit',
      text: 'Submit Arguments'
   }];

   public selectedOption = 'spark';
   public fields: StTableHeader[] = [
      { id: 'key', label: 'Key' },
      { id: 'value', label: 'Value' }
   ];


   constructor(private store: Store<fromRoot.State>, private _cd: ChangeDetectorRef) { }


   changeOrderArguments(event: any): void {
      this.orderByArguments = event.orderBy;
      this.sortOrderArguments = event.type;
   }

   changeOrderConfig(event: any): void {
      this.orderByConfig = event.orderBy;
      this.sortOrderConfig = event.type;
   }

   changeTableInfo(event: any): void {
      this.selectedOption = event.id;
   }

}
