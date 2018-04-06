/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, OnDestroy, NgZone, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { Store } from '@ngrx/store';

@Component({
    selector: 'selected-entity',
    styleUrls: ['selected-entity.styles.scss'],
    templateUrl: 'selected-entity.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class SelectedEntityComponent implements OnInit, OnDestroy {

    public topPosition: any = '0';
    public leftPosition: any = '0';

    constructor(private zone: NgZone, private _cd: ChangeDetectorRef) { }

    ngOnInit(): void {
        this.zone.runOutsideAngular(() => {
            this.mouseMove = this.mouseMove.bind(this);
            window.document.addEventListener('mousemove', this.mouseMove);
        });
    }

    mouseMove(event: any): void {
        this.topPosition = event.clientY + 'px';
        this.leftPosition = event.clientX + 'px';
        this._cd.detectChanges();
    }

    ngOnDestroy(): void {
        this._cd.detach();
        this.zone.runOutsideAngular(() => {
            window.document.removeEventListener('mousemove', this.mouseMove, false);
        });
    }
}
