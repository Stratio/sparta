/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy, ElementRef, ChangeDetectorRef, HostListener } from '@angular/core';

@Component({
  selector: 'sp-popover',
  templateUrl: './sp-popover.component.html',
  styleUrls: ['./sp-popover.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpPopoverComponent {

  @Input() get active(): boolean {
    return this._active;
  }

  set active(value: boolean) {
    this._active = value;
    if (value) {
      this.checkPosition();
    }
  }
  @Output() activeChange = new EventEmitter<boolean>();

  public leftPosition = false;
  public hiddenContent = true;

  private _active: boolean;
  constructor(private _eref: ElementRef, private _cd: ChangeDetectorRef) { }

  public showContent($event: any) {
    this.active = !this.active;
    this.activeChange.emit(this.active);
  }

  @HostListener('document:mousedown', ['$event'])
  public onClick(event: any): void {
    if (this.active) {
      // const searchBox = this.menuOptionsComponent.searchBox;
      if (!this._eref.nativeElement.contains(event.target)) {// or some similar check
        event.stopPropagation();
        this.activeChange.emit(this.active);
      }
    }
  }

  public checkPosition() {
    this.hiddenContent = true;
    this._cd.markForCheck();

    setTimeout(() => {
      const contentElement = this._eref.nativeElement.querySelector('.popover-content');
      const boundClient = contentElement.getBoundingClientRect();
      const leftDistance = boundClient.left + boundClient.width;
      this.leftPosition = (window.innerWidth - leftDistance) < 0;
      this.hiddenContent = false;
      this._cd.markForCheck();
    });
  }

}
