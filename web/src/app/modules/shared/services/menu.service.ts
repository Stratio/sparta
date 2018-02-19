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

import { StHeaderMenuOption } from '@stratio/egeo';
import { Injectable } from '@angular/core';
import { DASHBOARD_ROUTES } from './../../dashboard-route.enum';


@Injectable()
export class MenuService {

    private _menuOptions: StHeaderMenuOption[] = [
        {
            icon: 'icon-record',
            label: 'Workflows',
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
            label: 'Crossdata',
            link: DASHBOARD_ROUTES.CROSSDATA,
            subMenus: []
        },
        {
            icon: 'icon-record',
            label: 'Settings',
            link: DASHBOARD_ROUTES.SETTINGS,
            subMenus: [
                /* {
                     label:'GENERAL',
                     link: DASHBOARD_ROUTES.INPUTS,
                     isActive:true
                 },*/
                {
                    label: 'Backups',
                    link: DASHBOARD_ROUTES.BACKUPS
                },
                {
                    label: 'Plugins',
                    link: DASHBOARD_ROUTES.PLUGINS
                },
                {
                    label: 'Environment',
                    link: DASHBOARD_ROUTES.ENVIRONMENT
                }
            ]
        }
    ];

    getMenu(): StHeaderMenuOption[] {
        return this._menuOptions;
    }

}
