/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
   ChangeDetectionStrategy,
   Component,
   EventEmitter,
   Input,
   Output
} from '@angular/core';
import { Router } from '@angular/router';
import { StModalService } from '@stratio/egeo';

import { BreadcrumbMenuService } from 'services';
import { isWorkflowRunning } from '@utils';
import { MonitoringWorkflow } from '../../models/workflow';


@Component({
   selector: 'workflows-header',
   styleUrls: ['workflows-header.component.scss'],
   templateUrl: 'workflows-header.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowsHeaderComponent {

   @Input() selectedWorkflows: Array<MonitoringWorkflow> = [];
   @Input() showDetails = false;
   @Input() monitoringStatus: any = {};
   @Input() selectedFilter: any = '';
   @Input() searchQuery = '';
   @Input() workflowListLength: number;

   @Output() downloadWorkflows = new EventEmitter<void>();
   @Output() showWorkflowInfo = new EventEmitter<void>();
   @Output() onSelectFilter = new EventEmitter<string>();
   @Output() onSearch = new EventEmitter<string>();
   @Output() onRunWorkflow = new EventEmitter<MonitoringWorkflow>();
   @Output() onStopWorkflow = new EventEmitter<MonitoringWorkflow>();

   public breadcrumbOptions: string[] = [];
   public menuOptions: any = [];
   public isRunning = isWorkflowRunning;

   public filters: any = [{
      name: 'workflows',
      label: 'WORKFLOWS',
      value: ''
   },
   {
      name: 'running',
      label: 'RUNNING',
      value: 'Running'
   },
   {
      name: 'starting',
      label: 'STARTING',
      value: 'Starting'
   },
   {
      name: 'stopped',
      label: 'STOPPED',
      value: 'Stopped'
   },
   {
      name: 'failed',
      label: 'FAILED',
      value: 'Failed'
   }];

   constructor(private _modalService: StModalService,
      public breadcrumbMenuService: BreadcrumbMenuService,
      private route: Router) {

      this.breadcrumbOptions = ['Home'];
   }


   public runWorkflow(workflow: any): void {
      const policyStatus = workflow.status.status;
      if (isWorkflowRunning(policyStatus)) {
         this.onStopWorkflow.emit(workflow);
      } else {
         this.onRunWorkflow.emit(workflow);
      }
   }



   /* Launched, Starting, Started,  Stopping, Stopped, Finished, Killed,  NotStarted,  Uploaded, Created, Failed */
   /*
       Run: Stopped, Finished, Created, Failed, Killed, Stopping,
   
       Stop: Starting, Started, Uploaded, Launched, NotStarted
   */
   public editWorkflow(): void {
      this.route.navigate(['wizard', 'edit', this.selectedWorkflows[0].id]);
   }

   public selectFilter(filter: string): void {
      this.onSelectFilter.emit(filter);
   }

   public searchWorkflow($event: any): void {
      this.onSearch.emit($event.text);
   }

}
