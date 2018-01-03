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

import { Component, OnInit, Output, EventEmitter, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Store } from '@ngrx/store';
import * as workflowActions from 'actions/workflow';
import * as fromRoot from 'reducers';

@Component({
    selector: 'workflow-json-modal',
    templateUrl: './workflow-json-modal.template.html',
    styleUrls: ['./workflow-json-modal.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowJsonModal implements OnInit {

    @Output() onCloseJsonModal = new EventEmitter<string>();

    @ViewChild('jsonWorkflowForm') public jsonWorkflowForm: NgForm;

    public workflowValidationError = false;

    public model: any = {
        name: '',
        description: '',
        json: ''
    };

    constructor(private store: Store<fromRoot.State>) { }

    onChangedFile($event: string): void {
        try {
            let parsedJson = JSON.parse($event);
            this.model.name = parsedJson.name;
            this.model.description = parsedJson.description;
            this.model.json = JSON.stringify(JSON.parse($event), undefined, 3);
        } catch (error) {
            console.log('Parse error. Expected JSON file.');
        }
    }

    onSubmitWorkflow(): void {
        if (this.jsonWorkflowForm.valid) {
            let parsedJson = JSON.parse(this.model.json);
            let name = this.model.name;
            let description = this.model.description;

            /* override json value */
            if (name.length) {
                parsedJson.name = name;
            }

            if (description.length) {
                parsedJson.description = description;
            }

            this.store.dispatch(new workflowActions.SaveJsonWorkflowAction(parsedJson));
        }
    }

    ngOnInit() {

        this.store.select(fromRoot.getReloadState).subscribe((res: boolean) => {
            if (res) {
                this.store.dispatch(new workflowActions.ListWorkflowAction());
            }
        });
    }

}

