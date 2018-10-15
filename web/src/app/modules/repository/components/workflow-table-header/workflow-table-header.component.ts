/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, OnDestroy, Output } from '@angular/core';
import { WorkflowBreadcrumbItem } from './workflow-breadcrumb/workflow-breadcrumb.model';

@Component({
   selector: 'workflow-table-header',
   styleUrls: ['workflow-table-header.component.scss'],
   templateUrl: 'workflow-table-header.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowTableHeaderComponent implements  OnDestroy {

  @Input() levelOptions: Array<WorkflowBreadcrumbItem>;
  @Output() changeFolder = new EventEmitter<number>();

   ngOnDestroy(): void {

   }

   onSearchResult($event) {}
}
