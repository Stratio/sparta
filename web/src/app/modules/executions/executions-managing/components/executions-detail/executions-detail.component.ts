/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Output, EventEmitter, ChangeDetectorRef, Input, OnChanges } from '@angular/core';

@Component({
   selector: 'executions-detail',
   templateUrl: './executions-detail.template.html',
   styleUrls: ['./executions-detail.styles.scss']
})
export class ExecutionsDetailComponent implements OnInit, OnChanges {

   @Input() executionData: any;
   @Output() showWorkflowExecutionInfo = new EventEmitter<any>();
   @Output() showConsole = new EventEmitter<any>();


   public inputs: Array<string> = [];
   public outputs: Array<string> = [];
   public transformations: Array<string> = [];

   public lastError: any;
   public execution: any;

   ngOnChanges() {
      if (this.executionData) {
         this.execution = this.executionData && this.executionData.execution ? this.executionData.execution : null;
         this.lastError = this.executionData.lastError;
         this._cd.detectChanges();
      }

   }

   constructor(private _cd: ChangeDetectorRef) { }

   ngOnInit() { }

   onShowConsole() {
      this.showConsole.emit();
   }
}
