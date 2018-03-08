/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, Input, OnInit, Output, EventEmitter, ChangeDetectionStrategy, ElementRef } from '@angular/core';

@Component({
    selector: 'sp-popover',
    templateUrl: './sp-popover.template.html',
    styleUrls: ['./sp-popover.styles.scss'],
    host: {
        '(document:click)': 'onClick($event)'
    },
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpPopoverComponent implements OnInit {

    @Input() active = false;
    @Output() activeChange = new EventEmitter<boolean>();

    constructor(private _eref: ElementRef) { }

    ngOnInit() { }

    showContent($event: any) {
        this.active = !this.active;
        this.activeChange.emit(this.active);
    }

    onClick(event: any): void {
        if (this.active) {
            // const searchBox = this.menuOptionsComponent.searchBox;
            if (!this._eref.nativeElement.contains(event.target)) {// or some similar check
                this.active = false;
                this.activeChange.emit(this.active);
            }
        }
    }

}
