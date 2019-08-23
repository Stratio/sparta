/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  Component,
  ViewEncapsulation,
  OnInit,
  ElementRef,
  Input,
  HostListener,
  Output,
  EventEmitter,
  OnChanges,
} from '@angular/core';
import { select as d3Select } from 'd3-selection';

 @Component({
  selector: '[wizard-annotation-tip]',
  template: `
  <svg:g class="rocket-annotation-tip">
    <g class="annotation"></g>
  </svg:g>`,
  styleUrls: ['wizard-annotation-tip.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})
export class WizardAnnotationTipComponent implements OnChanges, OnInit {

  @Input() notificationNumber: number;
  @Input() color: string;
  @Input() openOnCreate: boolean;
  @Input() disableEvents: boolean;
  @Output() onMouseDown = new EventEmitter<{ x: number, y: number }>();

  private _circleElement: any;
  private _numberElement: any;

  constructor(private _elementRef: ElementRef) { }

  @HostListener('mousedown', ['$event'])
  onMousedown(event: MouseEvent) {
    if (!this.disableEvents) {
      event.stopPropagation();
    }
    this._openTip();
  }


   public ngOnInit(): void {
    if (this.openOnCreate) {
      setTimeout(() => this._openTip());
    }
    const annotation = d3Select(this._elementRef.nativeElement.querySelector('.annotation'))
      .append('g');

     annotation.append('circle')
      .attr('cx', 12)
      .attr('cy', 12)
      .attr('r', 12)
      .attr('transform', 'translate(-12,-11.5)')
      .attr('fill', 'rgba(0,0,0,0.25)')
      .style('cursor', 'pointer');

     this._circleElement = annotation.append('circle')
      .attr('cx', 11)
      .attr('cy', 11)
      .attr('r', 11)
      .attr('stroke', 'white')
      .attr('stroke-width', 1.5)
      .attr('transform', 'translate(-11,-11)')
      .attr('fill', this.color)
      .style('cursor', 'pointer');

     this._numberElement = annotation.append('text')
      .attr('class', 'annotation__label-text')
      .attr('y', 4)
      .text(this.notificationNumber);
  }

   public ngOnChanges(): void {
    if (this._circleElement) {
      this._circleElement.attr('fill', this.color);
      this._numberElement.text(this.notificationNumber);
    }
  }

   private _openTip() {
    const position = this._elementRef.nativeElement.getBoundingClientRect();
    this.onMouseDown.emit({
      x: position.left,
      y: position.top
    });
  }
 }
