/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
   Component, OnInit, Output, EventEmitter, Input, ElementRef,
   ChangeDetectionStrategy, ViewChild, ChangeDetectorRef
} from '@angular/core';
import { MenuOptionsComponent } from '@app/shared/components/floating-menu/menu-option/menu-options.component';
import { FloatingMenuModel } from './floating-menu.model';

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
            this.searchChange.emit('');
            this.menuOptionsComponent.searchBox.setValue('');
         }
      }
   }

   selectedMenuOption($event: any) {
      this.selectedOption.emit($event);
      this.showMenu = false;
      this.searchChange.emit('');
      this.menuOptionsComponent.searchBox.setValue('');
   }

   constructor(private _eref: ElementRef, private _cd: ChangeDetectorRef) { }
}


export interface FloatingMenuModel {
    name: string;
    value: any;
    icon?: string;
    stepType?: string;
    subMenus?: Array<FloatingMenuModel>;
}