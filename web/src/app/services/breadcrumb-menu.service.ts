/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Injectable()
export class BreadcrumbMenuService {

    public getOptions(lastOption?: string): string[] {
        let options = ['home'];
        const params = this.route.url.split('/');
        options = options.concat(params.slice(1, params.length));
        if (lastOption && lastOption.length) {
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
