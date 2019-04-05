/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
   ChangeDetectionStrategy,
   Component,
   Input,
   OnInit,
   Output,
   EventEmitter
} from '@angular/core';

@Component({
   selector: 'custom-execution-container',
   template: `
    <custom-execution [workflowName]="workflowName"
      [executionContexts]="executionContexts"
      [showSheduler]="showSheduler"
      [workflowId]="workflowId"
      [blockRunButton]="blockRunButton"
      (executeWorkflow)="executeWorkflow.emit($event)"
      (scheduleWorkflow)="scheduleWorkflow.emit($event)"
      (closeCustomExecution)="closeCustomExecution.emit()"></custom-execution>
  `,
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class CustomExecutionContainer implements OnInit {
   @Input() executionContexts: any;
   @Input() showSheduler: boolean;
   @Input() workflowName: string;
   @Input() workflowId: string;
   @Input() blockRunButton: boolean;
   @Output() closeCustomExecution = new EventEmitter();
   @Output() executeWorkflow = new EventEmitter<any>();
   @Output() scheduleWorkflow = new EventEmitter<any>();

   ngOnInit(): void {

   }


}
