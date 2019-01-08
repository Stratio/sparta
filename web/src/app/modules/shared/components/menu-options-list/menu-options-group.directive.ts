/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

 import { Directive, HostListener, EventEmitter, Output } from '@angular/core';

@Directive({
  selector: '[eventHandler]',
})
export class EventHandlerDirective {
  @Output() onKeyNavigationEvent = new EventEmitter<boolean>();
  @Output() onClickEvent = new EventEmitter();
  @Output() onEnterDown = new EventEmitter();
  @Output() onEscapeDown = new EventEmitter();

  @HostListener('document:keydown', ['$event'])
  onKeydownHandler(event: KeyboardEvent) {
    if (event.keyCode === 40 || event.keyCode === 38) {
      this.onKeyNavigationEvent.emit(event.keyCode === 40);
      event.preventDefault();
    } else if (event.keyCode === 13) {
      this.onEnterDown.emit();
      event.preventDefault();
    } else if (event.keyCode === 27) {
      this.onEscapeDown.emit();
      event.preventDefault();
    }
  }

  @HostListener('document:click', ['$event'])
  onClick(event: any): void {
    this.onClickEvent.emit(event);
  }
}
