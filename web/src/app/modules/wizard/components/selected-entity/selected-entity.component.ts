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

import { Component, OnInit, OnDestroy, HostListener, ElementRef, Input, AfterContentInit, NgZone, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';

import * as wizardActions from 'actions/wizard';

@Component({
    selector: 'selected-entity',
    styleUrls: ['selected-entity.styles.scss'],
    templateUrl: 'selected-entity.template.html'
})

export class SelectedEntityComponent implements OnInit, OnDestroy {

    public topPosition = '0';
    public leftPosition = '0';


    clickEvent(event: any) {
      /*  setTimeout(() => {
            this.store.dispatch(new wizardActions.DeselectedCreationEntityAction());
        },10000);*/

    }

    constructor(private zone: NgZone, private _cd: ChangeDetectorRef, private store: Store<fromRoot.State>) { }

    ngOnInit(): void {
        this.zone.runOutsideAngular(() => {
            this.mouseMove = this.mouseMove.bind(this);
            this.clickEvent = this.clickEvent.bind(this);
            window.document.addEventListener('mousemove', this.mouseMove);
            window.document.addEventListener('click', this.clickEvent);
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
            window.document.removeEventListener('click', this.clickEvent, false);
        });
    }
}
