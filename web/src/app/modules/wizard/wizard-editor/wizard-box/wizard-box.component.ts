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
    Component, OnInit, OnDestroy, HostListener, ElementRef, Input, AfterContentInit,
    ChangeDetectorRef, Output, EventEmitter, ChangeDetectionStrategy
} from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs/Rx';
import { Router, ActivatedRoute } from '@angular/router';
import * as base from 'assets/images/workflow-base.svg';
import * as d3 from 'd3';
import { ENTITY_BOX } from '../../wizard.constants';
import { UtilsService } from '@app/shared/services/utils.service';

@Component({
    selector: '[wizard-box]',
    styleUrls: ['wizard-box.styles.scss'],
    templateUrl: 'wizard-box.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardBoxComponent implements OnInit, OnDestroy, AfterContentInit {

    @Input() data: any;
    @Input() selected: boolean;

    @Output() onDrawConnector = new EventEmitter<any>();
    @Output() onFinishConnector = new EventEmitter<any>();

    private el: HTMLElement;
    private svg: any;
    private dragrect: any;
    private relationSelector: any;

    private isSelected = false;
    public boxConfig = ENTITY_BOX;
    public showConnector = false;
    public icon = '';
    public strokeColor = 'white';

    public relationClasses = '';

    constructor(elementRef: ElementRef, private utilsService: UtilsService, private _cd: ChangeDetectorRef) {
        this.el = elementRef.nativeElement;
        this.svg = d3.select(this.el);
    }

    ngOnInit(): void {
        this.icon = this.utilsService.getFragmentUnicodeIcon(this.data.modelType);
    }

    ngAfterContentInit() {

        this.relationSelector = d3.selectAll(this.el.querySelectorAll(('.relation')));
        switch (this.data.stepType) {
            case 'Input':
                this.relationClasses = 'output-point';
                this.generateEntry();
                break;
            case 'Output':
                this.relationClasses = 'entry-point';
                this.generateOutput();
                break;
            case 'Transformation':
                this.relationClasses = 'entry-point output-point';
                this.generateEntry(); this.generateOutput();
                break;
        }

    }

    generateEntry() {
        this.relationSelector.on('mousedown', () => {
            this.onDrawConnector.emit({
                event: d3.event,
                name: this.data.name
            });
            d3.event.stopPropagation();
        });
    }


    generateOutput() {
        this.relationSelector
            .on('mouseup', () => {
                this.onFinishConnector.emit(this.data.name);
            });
    }

    getEntityIcon() {

    }


    ngOnDestroy(): void {

    }
}
