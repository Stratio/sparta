/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
    Component, ElementRef, Input,
    ChangeDetectionStrategy, EventEmitter, Output, ChangeDetectorRef, SimpleChanges,
    AfterContentInit, OnChanges
} from '@angular/core';
import { Store } from '@ngrx/store';
import * as d3 from 'd3';

import * as fromRoot from 'reducers';
import * as wizardActions from 'actions/wizard';
import { ENTITY_BOX } from './../../../wizard.constants';
import { WizardNodePosition } from './../../../models/node';

@Component({
    selector: '[wizard-edge]',
    styleUrls: ['wizard-edge.styles.scss'],
    templateUrl: 'wizard-edge.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WizardEdgeComponent implements AfterContentInit, OnChanges {

    @Input() initialEntityName: string;
    @Input() finalEntityName: string;
    @Input() selectedSegment: any;
    @Input() index = 0;
    @Input() position1: WizardNodePosition;
    @Input() position2: WizardNodePosition;

    @Output() onRemoveSegment = new EventEmitter<any>();

    public segment = '';
    public isSelected = false;

    private h = ENTITY_BOX.height;
    private w = ENTITY_BOX.width;

    private svgPathVar: any;

    constructor(private elementRef: ElementRef, private store: Store<fromRoot.State>,
    private _cd: ChangeDetectorRef) { }

    ngAfterContentInit(): void {
        this.svgPathVar = d3.select(this.elementRef.nativeElement.querySelector('.svgPathVar'));
        this.getPosition(this.position1.x, this.position1.y, this.position2.x, this.position2.y );
        this._cd.detectChanges();
        this._cd.detach();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (!changes.position1 && !changes.position2) {
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
            this.getPosition(this.position1.x, this.position1.y, this.position2.x, this.position2.y );
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
}
