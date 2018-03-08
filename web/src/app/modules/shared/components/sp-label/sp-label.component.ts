/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
