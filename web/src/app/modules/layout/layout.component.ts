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
import { Subscription, Observable } from 'rxjs/Rx';
import { CustomAlert } from 'app/models/alert.model';
import { TranslateService } from '@ngx-translate/core';
import { Router, NavigationStart } from '@angular/router';
import * as errorsActions from 'actions/errors';

@Component({
    selector: 'layout',
    styleUrls: ['layout.styles.scss'],
    templateUrl: 'layout.template.html'
})

export class LayoutComponent implements OnInit, OnDestroy {

    public menu: Array<StHeaderMenuOption>;
    public showForbiddenError$: Observable<any>;

    private routeSubscription: Subscription;

    constructor(private menuService: MenuService, private router: Router, private store: Store<fromRoot.State>) {
       this.routeSubscription = router.events.subscribe((event) => {
            if (event instanceof NavigationStart) {
               this.store.dispatch(new errorsActions.ChangeRouteAction());
            }
       });
    }

    hideAlert() {
        this.store.dispatch(new errorsActions.ChangeRouteAction());
    }

    ngOnInit(): void {
        this.menu = this.menuService.getMenu();
        this.showForbiddenError$ = this.store.select(fromRoot.showPersistentError);
    }

    ngOnDestroy(): void { 
        this.routeSubscription && this.routeSubscription.unsubscribe();
    }



}
