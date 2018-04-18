/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnInit,
    Output,
    OnChanges,
    SimpleChanges,
    ChangeDetectorRef
} from '@angular/core';
import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';
import { Subscription } from 'rxjs/Subscription';
import { FormControl } from '@angular/forms';

@Component({
    selector: 'menu-options',
    templateUrl: './menu-options.template.html',
    styleUrls: ['./menu-options.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MenuOptionsComponent implements OnInit, AfterViewInit, OnChanges {
    @Input() debounce = 200;
    @Input() menuOptions: Array<FloatingMenuModel>;
    @Input() position = 'left';
    @Input() search = false;
    @Input() maxHeight = 1200;
    @Input() active = false;
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

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.active && changes.active.currentValue) {
            setTimeout(() => {
                this.scrollTopEnabled = this.scrollList.scrollTop > 0;
                this.scrollBottomEnabled = (this.scrollList.offsetHeight + this.scrollList.scrollTop) < this.scrollList.scrollHeight;
                this._cd.detectChanges();
            });
        }
    }

    selectOption(option: any) {
        this.selectedOption.emit(option);
    }

    ngOnInit() {
        this.manageSubscription();
    }

    onScroll(event: any) {
        this.scrollTopEnabled = event.target.scrollTop > 0;
        this.scrollBottomEnabled = (this.scrollList.offsetHeight + event.target.scrollTop) + 1 < event.target.scrollHeight;
    }

    showMenu(index: number, item: any, event: any) {
        if (item.subMenus) {
            this.menuPosition = event.target.offsetTop;
            this.maxHeightChild = window.innerHeight - event.target.getBoundingClientRect().top - 30;
        }
        item.active = true;
        this._cd.detectChanges();
    }

    hideMenu(item: any) {
        item.active = false;
    }

    scrollTop() {
        this._scrollHandler = setInterval(() => {
            this.scrollList.scrollTop -= 1;
        }, 5);
    }

    scrollBottom() {
        this._scrollHandler = setInterval(() => {
            this.scrollList.scrollTop += 1;
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

    constructor(private elementRef: ElementRef, private _cd: ChangeDetectorRef) { }

}
