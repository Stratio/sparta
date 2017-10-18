///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Component, OnInit, Output, EventEmitter, ViewChild, ChangeDetectionStrategy, 
    Input, OnDestroy, ChangeDetectorRef} from '@angular/core';
import { NgForm } from '@angular/forms';
import { Store } from '@ngrx/store';
import * as workflowActions from 'actions/workflow';
import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs/Rx';
import { StTableHeader } from '@stratio/egeo';

@Component({
    selector: 'workflow-execution-info',
    templateUrl: './workflow-execution-info.template.html',
    styleUrls: ['./workflow-execution-info.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowExecutionInfoComponent implements OnInit, OnDestroy {

    @Input() workflow: any;
    public executionInfo: any;
    private executionInfoSubscription: Subscription;
    public fields : StTableHeader[] = [
        { id: 'key', label: 'Key'},
        { id: 'value', label: 'Value' }
    ];

    @ViewChild('jsonWorkflowForm') public jsonWorkflowForm: NgForm;

    constructor(private store: Store<fromRoot.State>, private _cd: ChangeDetectorRef) { }


    ngOnInit() {
        this.store.dispatch(new workflowActions.GetExecutionInfoAction(this.workflow.id));
        this.executionInfoSubscription = this.store.select(fromRoot.getExecutionInfo).subscribe((executionInfo: any) => {
           this.executionInfo = executionInfo;
           this._cd.detectChanges();
        });

    }


    public ngOnDestroy(): void {
       this.executionInfoSubscription && this.executionInfoSubscription.unsubscribe();
    }
}

