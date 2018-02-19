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

import {
    Component, OnInit, ViewChild, ChangeDetectionStrategy,
    Input, OnDestroy, ChangeDetectorRef
} from '@angular/core';
import { NgForm } from '@angular/forms';
import { Store } from '@ngrx/store';
import * as workflowActions from './../../actions/workflow-list';
import * as fromRoot from 'reducers';
import { StTableHeader, StHorizontalTab } from '@stratio/egeo';

@Component({
    selector: 'workflow-execution-info',
    templateUrl: './workflow-execution-info.template.html',
    styleUrls: ['./workflow-execution-info.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowExecutionInfoComponent implements OnInit, OnDestroy {

    @Input() executionInfo: any;


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

    @ViewChild('jsonWorkflowForm') public jsonWorkflowForm: NgForm;

    constructor(private store: Store<fromRoot.State>, private _cd: ChangeDetectorRef) { }


    changeOrderArguments($event: any): void {
        this.orderByArguments = $event.orderBy;
        this.sortOrderArguments = $event.type;
    }

    changeOrderConfig($event: any): void {
        this.orderByConfig = $event.orderBy;
        this.sortOrderConfig = $event.type;
    }

    changeTableInfo($event: any): void {
        this.selectedOption = $event.id;
    }

    ngOnInit() {
    }

    closeWorkflowExecutinInfo() {
        this.store.dispatch(new workflowActions.CloseWorkflowExecutionInfoAction());
    }

    public ngOnDestroy(): void { }
}

