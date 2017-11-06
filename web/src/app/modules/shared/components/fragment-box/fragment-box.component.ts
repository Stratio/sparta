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

import { Component, OnDestroy, OnInit, Input, Output, EventEmitter, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { icons } from '@app/shared/constants/icons';


@Component({
    selector: 'fragment-box',
    styleUrls: ['fragment-box.styles.scss'],
    templateUrl: 'fragment-box.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FragmentBoxComponent implements OnInit, OnDestroy {

    @Input() isChecked: boolean;
    @Input() fragmentData: any;

    @Output() onChecked = new EventEmitter<any>();

    public icons: any = icons;
    ngOnInit(): void {}

    constructor(private _cd: ChangeDetectorRef) { }



    public ngOnDestroy(): void {

    }
}
