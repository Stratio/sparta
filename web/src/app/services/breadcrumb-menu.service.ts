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

import { Injectable } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Injectable()
export class BreadcrumbMenuService {

    public getOptions(lastOption?: string): string[] {
        let options = ['home'];
        const params = this.route.url.split('/');
        options = options.concat(params.slice(1, params.length));
        if(lastOption && lastOption.length) {
            options[options.length - 1] = lastOption;
        }
        return options;
    }

    public setRoute(routeIndex: number) {
        const routeParams = this.getOptions();
        let route = '';
        for (let i = 1; i < routeIndex + 1; i++) {
            route += '/' + routeParams[i];
        }
        this.route.navigate([route], {});
    }

    constructor(private route: Router, private currentActivatedRoute: ActivatedRoute) { }
}
