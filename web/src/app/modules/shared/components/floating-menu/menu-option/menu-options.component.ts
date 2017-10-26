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

import { Component, OnInit, Output, EventEmitter, Input, ElementRef, ChangeDetectionStrategy } from '@angular/core';
import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';
import { Subscription } from "rxjs/Rx";
import { FormControl } from "@angular/forms";

@Component({
    selector: 'menu-options',
    templateUrl: './menu-options.template.html',
    styleUrls: ['./menu-options.styles.scss']
})
export class MenuOptionsComponent implements OnInit {

    @Input() debounce: number = 200;
    @Input() menuOptions: Array<FloatingMenuModel>;
    @Input() position: string = 'left';
    @Input() search = false;
    @Output() selectedOption = new EventEmitter<any>();
    @Output() searchChange = new EventEmitter<string>();

    public searchBox: FormControl = new FormControl();

    public searchOption = '';
    private subscriptionSearch: Subscription | undefined = undefined;
    private subscriptionSearchClearButton: Subscription | undefined = undefined;

    selectOption(option: any) {
       this.selectedOption.emit(option);
    }

    ngOnInit() {
        this.manageSubscription();
    }

    showMenu(item: any) {
        item.active = true;
    }

    hideMenu(item: any) {
        item.active =  false;
    }

    private manageSubscription(): void {
        if (this.subscriptionSearch !== undefined) {
           this.subscriptionSearch.unsubscribe();
        }

        this.subscriptionSearch = this.searchBox
            .valueChanges
            .debounceTime(this.debounce)
            .subscribe((event) => this.searchChange.emit(this.searchBox.value));
     }

    constructor() { }
}
