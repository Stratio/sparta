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
    Component, OnInit, OnDestroy, HostListener, ElementRef, Input,
    ChangeDetectionStrategy, EventEmitter, Output
} from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs/Rx';
import { Router, ActivatedRoute } from '@angular/router';
import { ENTITY_BOX } from '../../wizard.constants';
import * as wizardActions from 'actions/wizard';

@Component({
    selector: '[wizard-segment]',
    styleUrls: ['wizard-segment.styles.scss'],
    templateUrl: 'wizard-segment.template.html'

})

export class WizardSegmentComponent implements OnInit, OnDestroy {

    @Input() initialEntity: any;
    @Input() finalEntity: any;
    @Output() onRemoveSegment = new EventEmitter<any>();
    @Input() selectedSegment: any;

    private h = ENTITY_BOX.height;
    private w = ENTITY_BOX.width;

    public closeIconPosition = {
        x: 0,
        y: 0
    };

    private el: HTMLElement;


    constructor(elementRef: ElementRef, private store: Store<fromRoot.State>) { }

    getPosition(init: any, final: any) {

        if (init.uiConfiguration.position && final.uiConfiguration.position) {

            let x1 = init.uiConfiguration.position.x;
            let y1 = init.uiConfiguration.position.y;

            let x2 = final.uiConfiguration.position.x;
            let y2 = final.uiConfiguration.position.y;



            const diff = Math.abs(x1 - x2);
            if (diff > this.w + 16) {
                y1 += this.h / 2;
                y2 += this.h / 2;

                if (x1 > x2) {
                    x2 += this.w;
                    x1 += 8;
                } else {
                    x1 += this.w;
                    x2 -= 0;
                }

                this.closeIconPosition.x = x1 - ((x1 - x2 + 25) / 2);
                this.closeIconPosition.y = y1 - ((y1 - y2 - 30) / 2);

                return 'M' + x2 + ',' + y2 + ' C' + x1 + ',' + y2 + ' ' + x2 + ',' + y1 + ' ' + x1 + ',' + y1;

            } else {

                x1 += this.w / 2;
                x2 += this.w / 2;

                this.closeIconPosition.x = x1 - ((x1 - x2 + 26) / 2);
                this.closeIconPosition.y = y1 - ((y1 - y2 - 100) / 2);

                if (y1 > y2) {
                    y2 += this.h;
                } else {
                    y1 += this.h;
                    y2 -= 0;
                }
                //linea recta cuando este muy proximo
                if (Math.abs(y1 - y2) < 20) {
                    console.log("cosas raras");
                }


                return 'M' + x2 + ',' + y2 + ' C' + x2 + ',' + y1 + ' ' + x1 + ',' + y2 + ' ' + x1 + ',' + y1;

            }
        } else {
            return '';
        }
    }

    selectSegment(event: any) {
        event.stopPropagation();
        this.store.dispatch(new wizardActions.UnselectEntityAction());
        if (this.selectedSegment && this.selectedSegment.origin === this.initialEntity.name
            && this.selectedSegment.destination === this.finalEntity.name) {
            this.store.dispatch(new wizardActions.UnselectSegmentAction());
        } else {
            this.store.dispatch(new wizardActions.SelectSegmentAction({
                origin: this.initialEntity.name,
                destination: this.finalEntity.name
            }));
        }

    }

    deleteSegment() {
        this.onRemoveSegment.emit({
            origin: this.initialEntity.name,
            destination: this.finalEntity.name
        });
    }


    ngOnInit(): void {

    }

    ngOnDestroy(): void {

    }
}
