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
  OnDestroy,
  Output,
  ViewEncapsulation
} from '@angular/core';
import { select as d3Select } from 'd3-selection';

@Component({
  selector: '[wizard-selector]',
  template: '',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})
export class WizardSelectorComponent implements AfterViewInit, OnDestroy {

  @Input() position: { x: number; y: number };
  @Output() finishSelection = new EventEmitter<any>();

  private _rect: any;
  private x: number;
  private y: number;

  constructor(private _cd: ChangeDetectorRef, private _el: ElementRef) {
    this._cd.detach();
    this._move = this._move.bind(this);
    this._mouseUp = this._mouseUp.bind(this);
    document.addEventListener('mouseup', this._mouseUp);
    document.addEventListener('mousemove', this._move);
  }

  ngAfterViewInit(): void {
    this.x = this.position.x;
    this.y = this.position.y;
    this._rect = d3Select(this._el.nativeElement)
      .append('rect')
      .attr('fill', 'rgba(20,20,20, 0.1)')
      .attr('stroke', '#333')
      .attr('stroke-width', '0.5')
      .attr('x', this.position.x)
      .attr('y', this.position.y);
  }

  ngOnDestroy(): void {
    document.removeEventListener('mousemove', this._move);
    document.removeEventListener('mouseup', this._mouseUp);
  }

  private _move(event) {
    console.log("a")
    const yt = event.clientY - 135;
    const diffX = this.position.x - event.clientX;
    const diffY = this.position.y - yt;

    const x = diffX < 0 ? this.position.x : event.clientX;
    const y = diffY < 0 ? this.position.y : yt;

    this._rect
      .attr('x', x)
      .attr('y', y)
      .attr('width', Math.abs(diffX) + 'px')
      .attr('height', Math.abs(diffY) + 'px');
  }

  private _mouseUp() {
    this.finishSelection.emit(<Element>this._rect.node().getBoundingClientRect());
  }
}
