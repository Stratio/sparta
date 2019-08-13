/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { AfterContentInit, Directive, ElementRef, EventEmitter, Input, Output, OnDestroy } from '@angular/core';
import { SchemaFieldPosition } from './../models/schema-fields';
import { Subject, Subscription } from 'rxjs';

@Directive({ selector: '[field-position]' })
export class FieldPositionDirective implements AfterContentInit, OnDestroy {

  @Input() recalcPositionSubject: Subject<any>;
  @Input() position: SchemaFieldPosition;
  @Input() containerElement: HTMLElement;
  @Output() positionChange = new EventEmitter<SchemaFieldPosition>();

  private _positionSubscription: Subscription;

  constructor(private _el: ElementRef) { }

  ngAfterContentInit(): void {
    this._positionSubscription = this.recalcPositionSubject.subscribe(() => this.getElementRelativePosition());
  }

  ngOnDestroy(): void {
    this._positionSubscription.unsubscribe();
  }

  getElementRelativePosition() {
    const parentPos = this._getElementOffset(this.containerElement);
    const elementPos = this._getElementOffset(this._el.nativeElement);
    this.positionChange.emit({
      x: elementPos.left - parentPos.left,
      y: elementPos.top - parentPos.top,
      height: this._el.nativeElement.offsetHeight
    });
  }


  private _getElementOffset(element) {
    const de = document.documentElement;
    const box = element.getBoundingClientRect();
    const top = box.top + window.pageYOffset - de.clientTop;
    const left = box.left + window.pageXOffset - de.clientLeft;
    return { top, left };
  }
}
