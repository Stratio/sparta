/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Directive, HostBinding, Input, HostListener, Output, EventEmitter } from '@angular/core';
import { DragEventsService } from './drag-events.service';

@Directive({
    selector: '[dragEvents]'
})
export class DragEventsDirective {
    @Input() position: any;
    @Output() positionChange = new EventEmitter<any>();

    private currentX = 0;
    private currentY = 0;

    constructor(private dragService: DragEventsService) {
    }

    @HostBinding('draggable')
    get draggable() {
        return true;
    }

    @Input()
    set myDraggable(options: DraggableOptions) {
        if (options) {
            this.options = options;
        }
    }

    private options: DraggableOptions = {};

    @HostListener('onmousedown', ['$event'])
    onMouseDown(event: any) {
        //const selectedElement = event.target;
        this.currentX = event.clientX;
        this.currentY = event.clientY;
    }

    @HostListener('dragstart', ['$event'])
    onDragStart(event: any) {
        const { zone = 'zone', data = {} } = this.options;

        this.dragService.startDrag(zone);

        event.dataTransfer.setData('Text', JSON.stringify(data));
    }

    @HostListener('drag', ['$event'])
    onDrag(event: any) {
        this.position = {
            x: event.layerX - this.currentX,
            y: event.layerY - this.currentY
        };
        this.positionChange.emit(this.position);
    }
}
export interface DraggableOptions {
    zone?: string;
    data?: any;
}