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
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnInit,
    Output
} from '@angular/core';
import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';
import { Subscription } from 'rxjs/Rx';
import { FormControl } from '@angular/forms';

@Component({
    selector: 'menu-options',
    templateUrl: './menu-options.template.html',
    styleUrls: ['./menu-options.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MenuOptionsComponent implements OnInit, AfterViewInit {

    @Input() debounce = 200;
    @Input() menuOptions: Array<FloatingMenuModel>;
    @Input() position = 'left';
    @Input() search = false;
    @Input() maxHeight = 1200;
    @Output() selectedOption = new EventEmitter<any>();
    @Output() searchChange = new EventEmitter<string>();

    public searchBox: FormControl = new FormControl();
    public menuPosition = 0;
    public searchOption = '';
    public maxHeightChild = 300;
    private subscriptionSearch: Subscription;

    public scrollTopEnabled = false;
    public scrollBottomEnabled = false;
    private scrollList: any;
    private _scrollHandler: any;

    ngAfterViewInit(): void {
        setTimeout(() => {
            this.scrollList = this.elementRef.nativeElement.querySelector('ul');
        });
    }

    selectOption(option: any) {
       this.selectedOption.emit(option);
    }

    ngOnInit() {
        this.manageSubscription();
    }

    onScroll(event: any) {
        this.scrollTopEnabled = event.srcElement.scrollTop > 0;
        this.scrollBottomEnabled = (this.scrollList.offsetHeight + event.srcElement.scrollTop) + 1 < event.srcElement.scrollHeight;
    }

    showMenu(index: number, item: any, event: any) {
        if (item.subMenus) {
            this.menuPosition = event.srcElement.offsetTop;
            this.maxHeightChild = window.innerHeight - event.srcElement.getBoundingClientRect().y - 30;
        }
        item.active = true;
    }

    showArrows() {
        this.scrollTopEnabled = this.scrollList.scrollTop > 0;
        this.scrollBottomEnabled = (this.scrollList.offsetHeight + this.scrollList.scrollTop) < this.scrollList.scrollHeight;
    }

    hideMenu(item: any) {
        item.active =  false;
    }

    scrollTop() {
        this._scrollHandler = setInterval(() => {

            this.scrollList.scrollTo(0, this.scrollList.scrollTop - 1);
        }, 5);
    }

    scrollBottom() {
        this._scrollHandler = setInterval(() => {

            this.scrollList.scrollTo(0, this.scrollList.scrollTop + 1);
        }, 4);
    }

    stopScroll() {
        clearInterval(this._scrollHandler);
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

    constructor(private elementRef: ElementRef) { }

}
