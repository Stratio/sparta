/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
    Component, OnInit, OnDestroy, ElementRef, Input, AfterContentInit,
    ChangeDetectorRef, Output, EventEmitter, ChangeDetectionStrategy, NgZone
} from '@angular/core';
import * as d3 from 'd3';

import { ENTITY_BOX } from './../../wizard.constants';
import { UtilsService } from '@app/shared/services/utils.service';
import { icons } from '@app/shared/constants/icons';
import { isMobile } from 'constants/global';
import { WizardNode } from '@app/wizard/models/node';
import { StepType } from '@models/enums';


@Component({
    selector: '[wizard-node]',
    styleUrls: ['wizard-node.styles.scss'],
    templateUrl: 'wizard-node.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WizardNodeComponent implements OnInit, OnDestroy, AfterContentInit {

    @Input() data: WizardNode;
    @Input() createdNew: boolean;
    @Input() selected: boolean;
    @Input() drawingConnectionStatus: any = {
        status: false,
        name: ''
    };

    @Output() onDrawConnector = new EventEmitter<any>();
    @Output() onFinishConnector = new EventEmitter<any>();

    private el: HTMLElement;
    private svg: d3.Selection<HTMLElement, any, any, any>;
    private relationSelector: d3.Selection<Element, any, any, any>;

    public boxConfig = ENTITY_BOX;
    public relationClasses = '';

    constructor(elementRef: ElementRef, private utilsService: UtilsService, private _cd: ChangeDetectorRef, private _ngZone: NgZone) {
        this.el = elementRef.nativeElement;
        this.svg = d3.select(this.el);
    }

    ngOnInit(): void {
        if (this.createdNew) {
            this.el.querySelector('.box').setAttribute('class', 'created-new');
        }
    }

    ngAfterContentInit() {
        setTimeout(() => {
            this.data.created = false;
        }, 400); //delete created fadeIn css effect
        const data = this.data;

        this._ngZone.runOutsideAngular(() => {
            const textContainer = d3.select(this.el.querySelector('.text-container'));
            textContainer.append('text')
                .attr('x', 20)
                .attr('y', 35)
                .attr('class', 'entity-icon')
                .style('font-size', '25')
                .attr('fill', this.createdNew ? '#999' : '#0f1b27')
                .text(function (d) { return icons[data.classPrettyName] });

            if (data.hasErrors && !this.createdNew) {
                textContainer.append('text')
                    .attr('x', 116)
                    .attr('y', 22)
                    .attr('class', 'entity-icon2')
                    .style('font-size', '16')
                    .attr('fill', '#ec445c')
                    .text(function (d) { return '\uE613'; });
            }
        });

        this.relationSelector = d3.selectAll(this.el.querySelectorAll(('.relation')));
        switch (this.data.stepType) {
            case StepType.Input:
                this.relationClasses = 'output-point';
                this.generateEntry();
                break;
            case StepType.Output:
                this.relationClasses = 'entry-point';
                this.generateOutput();
                break;
            case StepType.Transformation:
                this.relationClasses = 'entry-point output-point';
                this.generateEntry(); this.generateOutput();
                break;
        }
    }

    generateEntry() {
        const that = this;
        this.relationSelector.on('mousedown', function () {
            if (that.drawingConnectionStatus.status) {
                return;
            }
            d3.select(this)
                .classed('over-output2', true);
            that.onDrawConnector.emit({
                event: d3.event,
                name: that.data.name
            });
            d3.event.stopPropagation();
        });
    }


    generateOutput() {
        this._ngZone.runOutsideAngular(() => {
            this.relationSelector
                .on('mouseover', function () {
                    d3.select(this)
                        .classed('over-output', true);
                }).on('mouseout', function () {
                    d3.select(this).classed('over-output', false);
                }).on('mouseup', () => {
                    this.onFinishConnector.emit(this.data);
                });

                if (isMobile) {
                    this.relationSelector.on('click', () => {
                        if (this.drawingConnectionStatus.status && this.drawingConnectionStatus.name !== this.data.name) {
                            this.onFinishConnector.emit(this.data);
                        }
                    });
                }

        });
    }

    ngOnDestroy(): void { }
}


