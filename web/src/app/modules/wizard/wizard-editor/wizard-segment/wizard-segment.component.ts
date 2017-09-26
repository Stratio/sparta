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

import { Component, OnInit, OnDestroy, HostListener, ElementRef, Input, ChangeDetectionStrategy } from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs/Rx';
import { Router, ActivatedRoute } from '@angular/router';
import { ENTITY_BOX } from '../../wizard.constants';



@Component({
    selector: '[wizard-segment]',
    styleUrls: ['wizard-segment.styles.scss'],
    templateUrl: 'wizard-segment.template.html',

})

export class WizardSegmentComponent implements OnInit, OnDestroy {

    @Input() initialEntity: any;
    @Input() finalEntity: any;

    private h = ENTITY_BOX.height;
    private w = ENTITY_BOX.width;

    private el: HTMLElement;


    constructor(elementRef: ElementRef) {

    }

    getPosition(init: any, final: any) {

        if (init.uiConfiguration.position && final.uiConfiguration.position) {

            let x1 = init.uiConfiguration.position.x;
            let y1 = init.uiConfiguration.position.y;

            let x2 = final.uiConfiguration.position.x;
            let y2 = final.uiConfiguration.position.y;

            const diff = Math.abs(x1 - x2);
            if (diff > this.w) {
                y1 += this.h / 2;
                y2 += this.h / 2;

                if (x1 > x2) {
                    x2 += this.w;
                } else {
                    x1 += this.w;
                }

                return 'M' + x2 + ',' + y2 + ' C' + x1 + ',' + y2 + ' ' + x2 + ',' + y1 + ' ' + x1 + ',' + y1;

            } else {
                x1 += this.w / 2;
                x2 += this.w / 2;

                if (y1 > y2) {
                    y2 += this.h;
                } else {
                    y1 += this.h;
                }

                return 'M' + x1 + ',' + y1 + ' C' + x1 + ',' + y2 + ' ' + x2 + ',' + y1 + ' ' + x2 + ',' + y2;
            }
        } else {
            return '';
        }
    }


    ngOnInit(): void {

    }

    ngOnDestroy(): void {

    }
}
