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