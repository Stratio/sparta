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

import { Component, OnInit, Output, EventEmitter, ChangeDetectionStrategy, ChangeDetectorRef, Input } from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo';
import { inputsObject } from 'data-templates/inputs';
import { outputsObject } from 'data-templates/outputs';
import { transformationsObject } from 'data-templates/transformations';

@Component({
    selector: 'wizard-details',
    templateUrl: './wizard-details.template.html',
    styleUrls: ['./wizard-details.styles.scss']
})
export class WizardDetailsComponent implements OnInit {

    @Input() entityData: any;

    public templates: any = {};

    ngOnChanges() {

    }

    constructor(private _cd: ChangeDetectorRef) { 
        this.templates = {
            Input: inputsObject,
            Output: outputsObject,
            Transformation: transformationsObject
        };
    }

    ngOnInit() {

     }
}
