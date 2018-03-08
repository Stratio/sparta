/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { AnyFn } from '@ngrx/store/src/selector';
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

import { Component, OnInit, OnDestroy, NgZone, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';

@Component({
    selector: 'selected-entity',
    styleUrls: ['selected-entity.styles.scss'],
    templateUrl: 'selected-entity.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class SelectedEntityComponent implements OnInit, OnDestroy {

    public topPosition: any = '0';
    public leftPosition: any = '0';

    constructor(private zone: NgZone, private _cd: ChangeDetectorRef, private store: Store<fromRoot.State>) { }

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
