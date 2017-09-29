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

import { Component, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { StHeaderMenuOption , StFooterLink, StAlertsService, STALERT_SEVERITY } from '@stratio/egeo';
import { Store } from '@ngrx/store';
import { MenuService } from './../shared/services/menu.service';
import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs/Rx';
import { CustomAlert } from 'app/models/alert.model';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'layout',
    styleUrls: ['layout.styles.scss'],
    templateUrl: 'layout.template.html'
})

export class LayoutComponent implements OnInit, OnDestroy {

    public menu: Array<StHeaderMenuOption>;

    constructor(
        private menuService: MenuService) {
    }

    ngOnInit(): void {
        this.menu = this.menuService.getMenu();
    }

    ngOnDestroy(): void {
    }



}
