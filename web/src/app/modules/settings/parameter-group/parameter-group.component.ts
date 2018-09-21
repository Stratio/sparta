/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
   ChangeDetectionStrategy,
   Component,
   OnInit,
   ChangeDetectorRef,
} from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo/';
import { BreadcrumbMenuService } from 'app/services';
import { Observable } from 'rxjs/Observable';
import { Store } from '@ngrx/store';

import * as fromParameters from './reducers';
import * as alertParametersActions from './actions/alert';


@Component({
   selector: 'parameter-group',
   templateUrl: './parameter-group.component.html',
   styleUrls: ['./parameter-group.component.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class ParameterGroupComponent implements OnInit {

   public selectedOption = 'global';
   public breadcrumbOptions;
   public tabsOptions: StHorizontalTab[] = [
      {
         id: 'global',
         text: 'Global'
      }, {
         id: 'environment',
         text: 'Environment',
      },
      {
         id: 'custom',
         text: 'Custom'
      }
   ];
   public isLoading$: Observable<boolean>;
   public showAlert$: Observable<string>;
   public alertMessage = '';
   public showAlert: boolean;


   constructor(public breadcrumbMenuService: BreadcrumbMenuService, private _store: Store<fromParameters.State>, private _cd: ChangeDetectorRef) {
      this.breadcrumbOptions = breadcrumbMenuService.getOptions();
   }

   ngOnInit() {
      this.isLoading$ = this._store.select(fromParameters.isLoading);
      this._store.select(fromParameters.showAlert).subscribe(alert => {
         this.showAlert = !!alert;
         if (alert) {
            this.alertMessage = alert;
            this.closeAlert();
         } else {
            this._cd.markForCheck();
         }
      });
   }

   closeAlert() {
      setTimeout(() => this._store.dispatch(new alertParametersActions.HideAlertAction()), 30000);
   }

   changeTabOption(event: StHorizontalTab) {
      this.selectedOption = event.id;
   }

}
