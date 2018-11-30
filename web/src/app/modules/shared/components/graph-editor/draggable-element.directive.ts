/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { DOCUMENT } from '@angular/common';
import {
  AfterContentInit,
  Directive,
  ElementRef,
  EventEmitter,
  Inject,
  Input,
  NgZone,
  OnInit,
  Output
} from '@angular/core';
import * as d3 from 'd3';

import { event as d3Event } from 'd3-selection';
import { drag as d3Drag } from 'd3-drag';
import { select as d3Select } from 'd3-selection';

import { DraggableElementPosition } from './graph-editor.models';

@Directive({ selector: '[draggable-element]' })
export class DraggableElementDirective implements AfterContentInit, OnInit {

  @Input() position: DraggableElementPosition;

  @Output() positionChange = new EventEmitter<DraggableElementPosition>();
  @Output() onClickEvent = new EventEmitter();
  @Output() onDoubleClickEvent = new EventEmitter();
  @Output() setEditorDirty = new EventEmitter();

  private clicks = 0;
  private element: d3.Selection<any>;
  private lastUpdateCall: number;

  ngOnInit(): void {
    this.setPosition();
  }

  ngAfterContentInit() {
    this._ngZone.runOutsideAngular(() => {
      this.element
        .on('click', this.onClick.bind(this))
        .call(d3Drag()
          .on('drag', this.dragmove.bind(this))
          .on('start', () => {
            d3Event.sourceEvent.stopPropagation();
            this._document.body.classList.add('dragging');
          }).on('end', () => {
            this._document.body.classList.remove('dragging');
          }));
    });
  }

  dragmove() {
    const event = d3Event;
    this.position = {
      x: this.position.x + event.dx,
      y: this.position.y + event.dy
    };
    if (this.lastUpdateCall) {
      cancelAnimationFrame(this.lastUpdateCall);
      this.lastUpdateCall = null;
    }
    this._ngZone.run(() => {
      this.positionChange.emit(this.position);
      this.setEditorDirty.emit();
    });
    this.lastUpdateCall = requestAnimationFrame(this.setPosition.bind(this));
  }

  setPosition() {
    const value = `translate(${this.position.x},${this.position.y})`;
    this.element.attr('transform', value);
  }

  onClick() {
    d3Event.stopPropagation();
    this._ngZone.run(() => {
      this.clicks++;
      if (this.clicks === 1) {
        this.onClickEvent.emit();
        setTimeout(() => {
          if (this.clicks !== 1) {
            this.onDoubleClickEvent.emit();
          }
          this.clicks = 0;
        }, 200);
      }
    });

  }


  constructor(private elementRef: ElementRef,
    private _ngZone: NgZone,
    @Inject(DOCUMENT) private _document: Document,
  ) {
    this.element = d3Select(this.elementRef.nativeElement);
  }
}