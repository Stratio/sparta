/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { StHeaderMenuOption } from '@stratio/egeo';
import { Injectable } from '@angular/core';
import { DASHBOARD_ROUTES } from './../../dashboard-route.enum';


@Injectable()
export class MenuService {

    private _menuOptions: StHeaderMenuOption[] = [
        {
            icon: 'icon-record',
            label: 'Repository',
            link: DASHBOARD_ROUTES.WORKFLOWS_MANAGING,
            subMenus: []
        },
        {
            icon: 'icon-record',
            label: 'Templates',
            link: DASHBOARD_ROUTES.TEMPLATES,
            subMenus: [
                {
                    label: 'Inputs',
                    link: DASHBOARD_ROUTES.INPUTS
                },
                {
                    label: 'Transformations',
                    link: DASHBOARD_ROUTES.TRANSFORMATIONS
                },
                {
                    label: 'Outputs',
                    link: DASHBOARD_ROUTES.OUTPUTS
                }
            ]
        },
        {
            icon: 'icon-record',
            label: 'Catalog',
            link: DASHBOARD_ROUTES.CROSSDATA,
            subMenus: []
        }
    ];

    getMenu(): StHeaderMenuOption[] {
        return this._menuOptions;
    }

}
