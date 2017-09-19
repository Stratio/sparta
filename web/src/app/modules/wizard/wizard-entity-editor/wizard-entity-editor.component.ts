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
    Component, OnInit, OnDestroy, AfterViewChecked, ElementRef, ChangeDetectionStrategy,
    ChangeDetectorRef, HostListener
} from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs/Rx';
import { Router, ActivatedRoute } from '@angular/router';
import * as base from 'assets/images/workflow-base.svg';
import * as d3 from 'd3';
import * as wizardActions from 'actions/wizard';


@Component({
    selector: 'wizard-entity-editor',
    styleUrls: ['wizard-entity-editor.styles.scss'],
    templateUrl: 'wizard-entity-editor.template.html'
})

export class WizardEntityEditorComponent implements OnInit, OnDestroy {
    ngOnInit(): void {
        
    }


    ngOnDestroy(): void {

    }

}
