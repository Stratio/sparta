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

import { Component, ElementRef, HostBinding, HostListener, Input } from '@angular/core';


@Component({
    host: { class: 'st-label' },
    selector: '[sp-label]',
    styleUrls: ['./sp-label.styles.scss'],
    templateUrl: './sp-label.component.html'
})

export class SpLabelComponent {
    @HostBinding('class.st-tooltip') classTooltip: boolean;
    @HostBinding('class.st-tooltip-on') classTooltipOn: boolean;

    private _showOnClick: boolean;
    @Input()
    set showOnClick(value: boolean) {
        this._showOnClick = value;
        this.classTooltip = this.title && !value;
    }
    get showOnClick(): boolean {
        return this._showOnClick;
    }

    private _title: string;
    @Input('attr.title')
    set title(value: string) {
        this._title = value;
        if (value) {
            this.el.nativeElement.setAttribute('title', value);
            this.classTooltip = !this.showOnClick;
        } else {
            this.el.nativeElement.removeAttribute('title');
            this.classTooltip = false;
        }
    }
    get title(): string {
        return this._title;
    }

    @HostListener('document:click', ['$event']) onClick(event: Event): void {
        this.classTooltipOn = this.showOnClick && this.title && this.el.nativeElement.contains(event.target);
    }

    constructor(private el: ElementRef) {
        this.classTooltip = false;
        this.classTooltipOn = false;
        this.showOnClick = false;
        this.title = this.el.nativeElement.title;
    }
}
