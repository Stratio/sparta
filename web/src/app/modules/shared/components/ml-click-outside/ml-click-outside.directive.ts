/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Directive, EventEmitter, ElementRef, HostListener, Output } from '@angular/core';

@Directive({ selector: '[clickOutside]' })
export class StClickOutside {
  @Output() clickOutside: EventEmitter<MouseEvent> = new EventEmitter<MouseEvent>();

  constructor(private elementRef: ElementRef) {}

  @HostListener('document:click', ['$event'])
  public onDocumentClick(event: MouseEvent): void {
    const targetElement = event.target as HTMLElement;
      if (targetElement && !this.elementRef.nativeElement.contains(targetElement)) {
         this.clickOutside.emit(event);
      }
  }
}
