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

import { Component, ViewEncapsulation } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs/Rx';
import { CustomAlert } from 'app/models/alert.model';
import { Store } from '@ngrx/store';
import { StAlertsService } from '@stratio/egeo';

@Component({
    encapsulation: ViewEncapsulation.None,
    selector: 'st-app',
    styleUrls: ['./app.styles.scss'],
    templateUrl: './app.template.html'
})

export class AppComponent {
    constructor(private translate: TranslateService, private store: Store<fromRoot.State>, private _alertService: StAlertsService,) {
        let lang: string = navigator.language.split('-')[0];
        lang = /(es|en)/gi.test(lang) ? lang : 'en';
        translate.setDefaultLang('en');
        translate.use(lang);
    }

    ngOnInit(): void {
        this.store.select(fromRoot.getCurrentAlert).subscribe((alerts: any) => {
            if (alerts && alerts.length) {
                alerts.map((alert: any) => {
                    const title = 'ALERTS.' + alert.title;
                    const description = 'ALERTS.' + alert.description;
                    this.translate.get([title, description], alert.params).subscribe((value: { [key: string]: string }) => {
                        this._alertService.notifyAlert(value[title], value[description], alert.type, undefined, 1000);
                    });
                });
            }
        });
    }
}
