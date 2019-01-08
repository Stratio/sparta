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
  Output,
  HostListener
} from '@angular/core';

@Component({
  selector: 'menu-options-list',
  templateUrl: './menu-options-list.component.html',
  styleUrls: ['./menu-options-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MenuOptionsListComponent {

  @Input() options: MenuOptionListGroup[] = [];
  @Input() topPosition: number;
  @Input() rightPosition: number;
  @Output() selectedOption = new EventEmitter<String>();

  public showMenu = false;
  public position;

  public selectedGroup = 0;
  public selectedGroupItem = 0;
  public activeKeys = false;

  constructor(private _eref: ElementRef, private _cd: ChangeDetectorRef) { }

  activateMenu(event) {
    this.showMenu = !this.showMenu;
    this._resetKeyPosition();
  }

  onClick(event: any): void {
    if (this.showMenu && !this._eref.nativeElement.contains(event.target)) {
      this.showMenu = false;
      this._resetKeyPosition();
    }
  }

  onEscape() {
    this.showMenu = false;
  }

  onNavigate(downKey: boolean) {
    if (this.activeKeys) {
      if (downKey) {
        this.setNextItem();
      } else {
        this.setPreviousItem();
      }
    } else {
      this.activeKeys = true;
      if (!downKey) {
        this.setPreviousItem();
      }
    }
  }

  selectOption(option: MenuOption) {
    this.selectedOption.emit(option.id);
    this.showMenu = false;
    this._resetKeyPosition();
  }

  onEnterDown() {
    if (this.activeKeys) {
      this.selectOption(this.options[this.selectedGroup].options[this.selectedGroupItem]);
    }
  }

  enter(group: number, item: number) {
    this.activeKeys = true;
    this.selectedGroup = group;
    this.selectedGroupItem = item;
  }

  leave() {
    this._resetKeyPosition();
  }

  private setPreviousItem() {
    if (this.selectedGroupItem > 0) {
      this.selectedGroupItem -= 1;
      return;
    }
    if (this.selectedGroup > 0) {
      this.selectedGroup -= 1;
    } else {
      this.selectedGroup = this.options.length - 1;
    }
    // select the last one of the group
    this.selectedGroupItem = this.options[this.selectedGroup].options.length - 1;
  }

  private setNextItem() {
    if (this.selectedGroupItem < this.options[this.selectedGroup].options.length - 1) {
      this.selectedGroupItem += 1;
      return;
    }
    if (this.selectedGroup === this.options.length - 1) {
      this.selectedGroup = 0;
    } else {
      this.selectedGroup += 1;
    }
    // select the first one of the group
    this.selectedGroupItem = 0;
  }

  private _resetKeyPosition() {
    this.selectedGroup = 0;
    this.selectedGroupItem = 0;
    this.activeKeys = false;
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
