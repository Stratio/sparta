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

import { Directive, Output, EventEmitter, AfterContentInit, ElementRef, OnInit, Input, NgZone } from '@angular/core';
import * as d3 from 'd3';

@Directive({ selector: '[svg-draggable]' })
export class DraggableSvgDirective implements AfterContentInit, OnInit {

    @Input() position: any;
    @Output() positionChange = new EventEmitter<any>();
    @Output() onClickEvent = new EventEmitter();
    @Output() onDoubleClickEvent = new EventEmitter();

    private clicks = 0;
    private element: any;

    ngOnInit(): void {
        const value = 'translate(' + this.position.x + ',' + this.position.y + ')';
        this.element.attr('transform', value);
    }

    ngAfterContentInit() {
        this.element
            .on('click', this.onClick.bind(this))
            .call(d3.drag().on('drag', this.dragmove.bind(this)));
    }

    dragmove(e: any, f: any) {
        const event = d3.event;
        this.position = {
            x: this.position.x + event.dx,
            y: this.position.y + event.dy
        };
        const value = 'translate(' + this.position.x + ',' + this.position.y + ')';
        this.element.attr('transform', value);
        this.positionChange.emit(this.position);
    }

    onClick() {
        d3.event.stopPropagation();
        this.clicks++;
        if (this.clicks === 1) {
            setTimeout(() => {
                if (this.clicks === 1) { //single click
                    this.onClickEvent.emit();
                } else {                //double click
                    this.onDoubleClickEvent.emit();
                }
                this.clicks = 0;
            }, 200);
        }
    }


    constructor(private elementRef: ElementRef, private zone: NgZone) {
        this.element = d3.select(this.elementRef.nativeElement);
    }
}
