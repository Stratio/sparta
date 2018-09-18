/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

@Component({
    selector: 'menu-options-list',
    templateUrl: './menu-options-list.component.html',
    styleUrls: ['./menu-options-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
        host: {
        '(document:click)': 'onClick($event)'
    }
})
export class MenuOptionsListComponent  {
   @Input() options: MenuOptionListGroup[] = [];
   @Input() topPosition: number;
   @Input() rightPosition: number;
   @Output() selectedOption = new EventEmitter<String>();
   public showMenu = false;
   public position;

   constructor(private _eref: ElementRef, private _cd: ChangeDetectorRef) { }

   activateMenu(event) {
     this.showMenu = !this.showMenu;
   }

   onClick(event: any): void {
     if (this.showMenu && !this._eref.nativeElement.contains(event.target)) {
      this.showMenu = false;
     }
   }

   selectOption(option: MenuOption) {
    this.selectedOption.emit(option.id);
    this.showMenu = false;
   }
}

export interface MenuOptionListGroup {
  options: MenuOption[];
}

export interface MenuOption {
  id: String;
  icon?: String;
  label: String;
  color?: String;
}
