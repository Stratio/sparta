/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Store, select } from '@ngrx/store';

import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';


import * as fromWizard from './../../reducers';
import * as wizardActions from './../../actions/wizard';


@Component({
  selector: 'wizard-notifications',
  styleUrls: ['wizard-notifications.styles.scss'],
  templateUrl: 'wizard-notifications.template.html'
})

export class WizardNotificationsComponent implements OnInit, OnDestroy {

  public notification: any;
  private _componentDestroyed = new Subject();

  constructor(private _store: Store<fromWizard.State>,
              private _cd: ChangeDetectorRef) { }

  ngOnInit(): void {
    let handler;
    this._store.pipe(select(fromWizard.getWizardNofications))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((notification: any) => {
        if ((notification.message && notification.message.length) || notification.templateType) {
          this.notification = {
            ...this.notification,
            visible: false
          };
          this._cd.markForCheck();
          clearTimeout(handler);
          setTimeout(() => {
            this.notification = {
              ...notification,
              visible: true
            };
            this._cd.markForCheck();
            handler = setTimeout(() => {
              this.notification = {
                ...this.notification,
                visible: false
              };
              this._cd.markForCheck();
            }, notification.time === 0 ? 10000000 : (notification.time || 4000));
          });
        } else {
          this.notification = {};
        }
      });
  }

  showGlobalErrors() {
    this._store.dispatch(new wizardActions.ShowGlobalErrorsAction());
  }


  ngOnDestroy(): void {
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
  }
}
