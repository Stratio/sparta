/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, ViewEncapsulation, OnInit, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs';
import { Store, select } from '@ngrx/store';
import { StAlertsService } from '@stratio/egeo';

@Component({
    encapsulation: ViewEncapsulation.None,
    selector: 'st-app',
    styleUrls: ['./app.styles.scss'],
    templateUrl: './app.template.html'
})

export class AppComponent implements OnInit, OnDestroy {

    private _alertSubscription: Subscription;

    constructor(private _translate: TranslateService, private _store: Store<fromRoot.State>, private _alertService: StAlertsService, ) {
        let lang: string = navigator.language.split('-')[0];
        lang = /(es|en)/gi.test(lang) ? lang : 'en';
        _translate.setDefaultLang('en');
        _translate.use('en');
    }

    ngOnInit(): void {
        this._alertSubscription = this._store.pipe(select(fromRoot.getCurrentAlert)).subscribe((alerts: any) => {
            if (alerts && alerts.length) {
                alerts.map((alertNot: any) => {
                    if (alertNot.notranslate) {
                        this._alertService.notifyAlert(alertNot.title, alertNot.description, alertNot.type,
                        undefined, alertNot.duration ? alertNot.duration : 1000);
                    } else {
                        const title = 'ALERTS.' + alertNot.title;
                        const description = alertNot.text ? alertNot.text : 'ALERTS.' + alertNot.description;
                        this._translate.get([title, description], alertNot.params).subscribe((value: { [key: string]: string }) => {
                            this._alertService.notifyAlert(value[title], value[description], alertNot.type,
                            undefined, alertNot.duration ? alertNot.duration : 1000);
                        });
                    }
                });
            }
        });
    }

    ngOnDestroy(): void {
        this._alertSubscription && this._alertSubscription.unsubscribe();
    }
}
