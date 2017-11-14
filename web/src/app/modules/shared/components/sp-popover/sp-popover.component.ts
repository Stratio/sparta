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
