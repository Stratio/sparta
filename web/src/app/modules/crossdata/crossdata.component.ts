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

import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';

import { BreadcrumbMenuService } from 'services';

@Component({
    selector: 'crossdata',
    templateUrl: './crossdata.template.html',
    styleUrls: ['./crossdata.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CrossdataComponent {

    public options: Array<any> = [
        {
            i: 0,
            id: 'catalog',
            text: 'CATALOG',
            description: 'CATALOG_DESCRIPTION'
        },
        {
            i: 1,
            id: 'queries',
            text: 'QUERIES',
            description: 'QUERIES_DESCRIPTION'
        }
    ];
    public activeMenuOption: any = this.options[0];
    public breadcrumbOptions: Array<any>;


    public onChangedOption(event: string) {
        this.activeMenuOption = event;
        this._cd.markForCheck();
    }

    constructor(public breadcrumbMenuService: BreadcrumbMenuService, private _cd: ChangeDetectorRef) {
        this.breadcrumbOptions = breadcrumbMenuService.getOptions();
    }
}
