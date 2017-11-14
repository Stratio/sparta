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

import {
    Component, OnInit, Output, EventEmitter, Input, ElementRef,
    ChangeDetectionStrategy, ViewChild, ChangeDetectorRef
} from '@angular/core';
import { MenuOptionsComponent } from '@app/shared/components/floating-menu/menu-option/menu-options.component';

@Component({
    selector: 'floating-menu',
    templateUrl: './floating-menu.template.html',
    styleUrls: ['./floating-menu.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        '(document:click)': 'onClick($event)'
    }
})
export class FloatingMenuComponent implements OnInit {

    @Input()
    get menuOptions() {
        return this._menuOptions;
    }
    set menuOptions(menuOptions: Array<FloatingMenuModel>) {
        this._menuOptions = menuOptions;
    }

    @Input() position = 'left';
    @Input() search = false;

    @Output() selectedOption = new EventEmitter<any>();
    @Output() searchChange = new EventEmitter<string>();
    @ViewChild(MenuOptionsComponent) menuOptionsComponent: MenuOptionsComponent;

    public showMenu = false;
    private _menuOptions: Array<FloatingMenuModel> = [];

    ngOnInit() { }

    activateMenu(event: any) {
        this.showMenu = !this.showMenu;
    }

    onClick(event: any): void {
        if (this.showMenu) {
            // const searchBox = this.menuOptionsComponent.searchBox;
            if (!this._eref.nativeElement.contains(event.target)) {// or some similar check
                this.showMenu = false;
                this.menuOptionsComponent.searchBox.setValue('');
            }
        }
    }

    selectedMenuOption($event: any) {
        this.selectedOption.emit($event);
        this.showMenu = false;
        this.menuOptionsComponent.searchBox.setValue('');
    }

    constructor(private _eref: ElementRef, private _cd: ChangeDetectorRef) { }
}


export interface FloatingMenuModel {
    name: string;
    value: any;
    icon?: string;
    subMenus?: Array<FloatingMenuModel>;
}
