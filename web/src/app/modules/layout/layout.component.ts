/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { StHeaderMenuOption } from '@stratio/egeo';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { MenuService } from './../shared/services/menu.service';
import * as fromRoot from 'reducers';
import { Router, NavigationStart } from '@angular/router';
import * as errorsActions from 'actions/errors';


@Component({
   selector: 'layout',
   styleUrls: ['layout.styles.scss'],
   templateUrl: 'layout.template.html'
})

export class LayoutComponent implements OnInit, OnDestroy {

   public userName = '';
   public menu: Array<StHeaderMenuOption>;
   public showForbiddenError$: Observable<any>;

   private routeSubscription: Subscription;
   private usernameSubscription: Subscription;

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

   redirectHome() {
      this.router.navigate(['']);
   }

   ngOnInit(): void {
      this.menu = this.menuService.getMenu();
      this.showForbiddenError$ = this.store.select(fromRoot.showPersistentError);
      this.usernameSubscription = this.store.select(fromRoot.getUsername).subscribe((userName: string) => {
         this.userName = userName;
      });
   }

   ngOnDestroy(): void {
      this.routeSubscription && this.routeSubscription.unsubscribe();
      this.usernameSubscription && this.usernameSubscription.unsubscribe();
   }

}
