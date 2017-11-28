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
    Component, OnInit, OnDestroy, ElementRef, Input,
    ChangeDetectionStrategy, EventEmitter, Output, ChangeDetectorRef, SimpleChanges
} from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { ENTITY_BOX } from '../../wizard.constants';
import * as d3 from 'd3';
import * as wizardActions from 'actions/wizard';
import { AfterContentInit, OnChanges } from '@angular/core';

@Component({
    selector: '[wizard-edge]',
    styleUrls: ['wizard-edge.styles.scss'],
    templateUrl: 'wizard-edge.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WizardEdgeComponent implements OnInit, AfterContentInit, OnChanges, OnDestroy {


    @Input() initialEntityName: string;
    @Input() finalEntityName: string;
    @Output() onRemoveSegment = new EventEmitter<any>();
    @Input() selectedSegment: any;
    @Input() index = 0;
    @Input() position: any;

    public segment = '';
    public isSelected = false;

    private h = ENTITY_BOX.height;
    private w = ENTITY_BOX.width;

    private svgPathVar: any;


    constructor(private elementRef: ElementRef, private store: Store<fromRoot.State>,
    private _cd: ChangeDetectorRef) { }

    ngAfterContentInit(): void {
        this.svgPathVar = d3.select(this.elementRef.nativeElement.querySelector('.svgPathVar'));
        this.getPosition(this.position.e1.x, this.position.e1.y, this.position.e2.x, this.position.e2.y );
        this._cd.detectChanges();
        this._cd.detach();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (!changes.position) {
            if (changes.selectedSegment) {
                if (this.selectedSegment && this.selectedSegment.origin === this.initialEntityName
                    && this.selectedSegment.destination === this.finalEntityName) {
                    this.isSelected = true;
                } else {
                    this.isSelected = false;
                }
            }
            this._cd.detectChanges();
        } else {
            this.getPosition(this.position.e1.x, this.position.e1.y, this.position.e2.x, this.position.e2.y );
        }
    }

    getPosition(x1: number, y1: number, x2: number, y2: number) {
        if (!this.svgPathVar) {
            return;
        }

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

            this.svgPathVar.attr('d', 'M' + x2 + ',' + y2 + ' C' + x1 + ',' + y2 + ' ' + x2 + ',' + y1 + ' ' + x1 + ',' + y1);

        } else {

            x1 += this.w / 2;
            x2 += this.w / 2;

            if (y1 > y2) {
                y2 += this.h;
            } else {
                y1 += this.h;
                y2 -= 0;
            }

            this.svgPathVar.attr('d', 'M' + x2 + ',' + y2 + ' C' + x2 + ',' + y1 + ' ' + x1 + ',' + y2 + ' ' + x1 + ',' + y1);
        }
    }

    selectSegment(event: any) {
        event.stopPropagation();
        this.store.dispatch(new wizardActions.UnselectEntityAction());
        if (this.selectedSegment && this.selectedSegment.origin === this.initialEntityName
            && this.selectedSegment.destination === this.finalEntityName) {
            this.store.dispatch(new wizardActions.UnselectSegmentAction());
        } else {
            this.store.dispatch(new wizardActions.SelectSegmentAction({
                origin: this.initialEntityName,
                destination: this.finalEntityName
            }));
        }
    }


    ngOnInit(): void {

    }

    ngOnDestroy(): void {

    }
}
