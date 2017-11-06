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

import { Component, OnDestroy, OnInit, Input, Output, EventEmitter, ChangeDetectionStrategy, ChangeDetectorRef, HostListener } from '@angular/core';
import { icons } from '@app/shared/constants/icons';
import { inputsObject } from 'data-templates/inputs';
import { outputsObject } from 'data-templates/outputs';
import { transformationsObject } from 'data-templates/transformations';

@Component({
    selector: 'info-fragment',
    styleUrls: ['info-fragment.styles.scss'],
    templateUrl: 'info-fragment.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class InfoFragmentComponent implements OnInit, OnDestroy {

    @Input() stepType: string;
    @Input() classPrettyName: string;

    public fragmentInfo = '';
    public showInfo = false;

    @HostListener('document:click') onClick() {
        this.showInfo = false;
    }

    ngOnInit(): void {
        switch (this.stepType) {
            case 'input':
                this.fragmentInfo = inputsObject[this.classPrettyName].description;
                break;
            case 'output':
                this.fragmentInfo = outputsObject[this.classPrettyName].description;
                break;
            case 'transformation':
                this.fragmentInfo = transformationsObject[this.classPrettyName].description;
                break;
        }
    }

    constructor() { }

    showFragmentTypeInfo(event: any) {
        event.stopPropagation();
        this.showInfo = true;
    }


    public ngOnDestroy(): void {

    }
}
