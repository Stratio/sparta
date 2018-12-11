/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Output, EventEmitter, ChangeDetectorRef, Input, OnChanges } from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo';

@Component({
   selector: 'executions-sidebar-detail',
   templateUrl: './executions-sidebar-detail.template.html',
   styleUrls: ['./executions-sidebar-detail.styles.scss']
})
export class ExecutionsSidebarDetailComponent implements OnInit, OnChanges {

   @Input() executionData: any;
   @Output() showWorkflowExecutionInfo = new EventEmitter<any>();
   @Output() showConsole = new EventEmitter<any>();


   public inputs: Array<string> = [];
   public outputs: Array<string> = [];
   public transformations: Array<string> = [];

   public objectKeys = Object.keys;

   public lastError: any;
   public execution: any;
   public executionContext: any;
   public parametersList2: any;
   public openParameters: any = { Global: true };

  public options: StHorizontalTab[] = [
    {
      id: 'overview',
      text: 'Overview'
    }, {
      id: 'parameters',
      text: 'Parameters'
    }
  ];
  public activeOption = this.options[0];

  constructor(private _cd: ChangeDetectorRef) { }

  ngOnInit() { }

  ngOnChanges() {
    if (this.executionData) {
      const parametersList = this.executionData.genericDataExecution.workflow.parametersUsedInExecution;
      this.execution = this.executionData && this.executionData.execution ? this.executionData.execution : null;
      this.lastError = this.executionData.lastError;
      this.parametersList2 = {};

      Object.keys(parametersList).forEach(key => {
        this.parametersList2[key.split('.')[0]] = [...this.parametersList2[key.split('.')[0]] || [], { name: key.split('.')[1], value: parametersList[key] }];
      });
      this._cd.detectChanges();
    }

  }
  toggleParameters(type) {
    this.openParameters[type] = !this.openParameters[type];
  }
  changedOption(event: StHorizontalTab) {
    this.activeOption = event;
  }

  onShowConsole() {
    this.showConsole.emit();
  }
}
