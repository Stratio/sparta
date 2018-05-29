/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    Output
} from '@angular/core';

@Component({
    selector: 'sparta-sidebar',
    templateUrl: './sparta-sidebar.template.html',
    styleUrls: ['./sparta-sidebar.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpartaSidebarComponent implements AfterViewInit {

    @Input() isVisible = false;
    @Input() showScrollBar = false;
    @Output() onCloseSidebar = new EventEmitter();

    public minHeight: number;

    constructor(private _element: ElementRef) { }

    ngAfterViewInit(): void {
        const rect = this._element.nativeElement.getBoundingClientRect();
        this.minHeight = window.innerHeight - rect.top - 30;
    }
}
