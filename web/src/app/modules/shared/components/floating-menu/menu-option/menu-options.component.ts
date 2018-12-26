/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { FloatingMenuModel } from './../floating-menu.model';

@Component({
   selector: 'menu-options',
   templateUrl: './menu-options.template.html',
   styleUrls: ['./menu-options.styles.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class MenuOptionsComponent implements OnInit, AfterViewInit {

   @Input() menuOptions: Array<FloatingMenuModel>;
   @Input() search = false;
   @Input() position = 'left';
   @Input() maxHeight = 1200;
   @Input() debounce = 200;

   @Output() selectedOption = new EventEmitter<any>();
   @Output() searchChange = new EventEmitter<string>();

   public searchBox: FormControl = new FormControl();
   public searchOption = '';
   public openedItem = '';

   public menuPosition = 0;
   public maxHeightChild = 300;

   public scrollTopEnabled = false;
   public scrollBottomEnabled = false;
   private scrollList: any;

   private _scrollHandler: any;
   private subscriptionSearch: Subscription;

   constructor(private elementRef: ElementRef, private _cd: ChangeDetectorRef) { }

   ngOnInit(): void {
      this.manageSubscription();
   }

   ngAfterViewInit(): void {
      setTimeout(() => {
         this.scrollList = this.elementRef.nativeElement.querySelector('ul');
         this.scrollTopEnabled = this.scrollList.scrollTop > 0;
         this.scrollBottomEnabled = (this.scrollList.offsetHeight + this.scrollList.scrollTop) < this.scrollList.scrollHeight;
         this._cd.markForCheck();
      });
   }

   onScroll(event: any) {
     console.log(event)
      this.scrollTopEnabled = event.target.scrollTop > 0;
      this.scrollBottomEnabled = (this.scrollList.offsetHeight + event.target.scrollTop) + 1 < event.target.scrollHeight;
   }

   showMenu(event, item) {
      if (this.openedItem !== item.name + (item.icon || '')) {
         this.openedItem = item.name  + (item.icon || '');
         if (item.subMenus) {
            this.menuPosition = event.target.offsetTop;
            this.maxHeightChild = window.innerHeight - event.target.getBoundingClientRect().top - 30;
         }
         this._cd.markForCheck();
      }
   }

   trackByFn(index, item) {
      return item.value + (item.icon || ''); // or item.id
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
         .pipe(debounceTime(this.debounce))
         .subscribe((event) => this.searchChange.emit(this.searchBox.value));
   }

}
