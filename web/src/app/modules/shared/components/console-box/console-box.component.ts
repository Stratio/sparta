/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnDestroy, OnInit, ChangeDetectionStrategy, Output, ElementRef, NgZone, Inject, EventEmitter, AfterViewInit } from '@angular/core';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'console-box',
  styleUrls: ['console-box.styles.scss'],
  templateUrl: 'console-box.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConsoleBoxComponent implements OnInit, AfterViewInit, OnDestroy {

  private pos1 = 0;
  private pos2 = 0;
  private _element: any;

  ngOnInit(): void { }

  constructor(
    private _el: ElementRef,
    private _ngZone: NgZone,
    @Inject(DOCUMENT) private _document: Document) {
    this._element = this._el.nativeElement;
    this._element.style.transform = 'translateY(100%)';
  }

  moveBox(e) {
    this._document.body.classList.add('dragging-console');
    this._ngZone.runOutsideAngular(() => {
      this.pos2 = e.clientY;
      document.onmouseup = this._closeDragElement.bind(this);
      document.onmousemove = this._elementDrag.bind(this);
    });
  }
  ngAfterViewInit(): void {
    this._element.style.top = window.innerHeight - 200 + 'px';
    setTimeout(() => {
      this._element.style.transform = 'translateY(0)';
    });
  }

  private _elementDrag(e) {
    // calculate the new cursor position:
    this.pos1 = this.pos2 - e.clientY;
    this.pos2 = e.clientY;
    // set the element's new position:
    const top = this._element.offsetTop - this.pos1;
    const maxH = 50;
    this._element.style.top = top < maxH ? maxH : top + 'px';
  }

  private _closeDragElement() {
    this._document.body.classList.remove('dragging-console');
    /* stop moving when mouse button is released:*/
    document.onmouseup = null;
    document.onmousemove = null;
  }

  public ngOnDestroy(): void {

  }
}
