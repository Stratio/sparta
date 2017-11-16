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
            icon: 'icon-input',
            label: 'Workflows',
            link: DASHBOARD_ROUTES.WORKFLOWS,
            subMenus: []
        },
        {
            icon: 'icon-puzzle',
            label: 'Templates',
            link: DASHBOARD_ROUTES.TEMPLATES,
            subMenus: [
                {
                    label: 'Inputs',
                    link: DASHBOARD_ROUTES.INPUTS
                },
                {
                    label: 'Outputs',
                    link: DASHBOARD_ROUTES.OUTPUTS
                }/*,
                {
                    label: 'Transformations',
                    link: DASHBOARD_ROUTES.TRANSFORMATIONS
                }*/
            ]
        },
        {
            icon: 'icon-cog',
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
                /*  {
                      label:'Resources',
                      link: DASHBOARD_ROUTES.RESOURCES
                  },*/
                {
                    label: 'Crossdata',
                    link: DASHBOARD_ROUTES.CROSSDATA
                }
            ]
        }
    ];

    getMenu(): StHeaderMenuOption[] {
        return this._menuOptions;
    }


    private findSection(label: string): StHeaderMenuOption {
        return this._menuOptions.find(item => item.label === label);
    }
}
