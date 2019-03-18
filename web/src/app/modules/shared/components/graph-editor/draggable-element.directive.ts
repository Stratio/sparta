/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  AfterContentInit,
  Directive,
  ElementRef,
  EventEmitter,
  Input,
  NgZone,
  OnInit,
  Output,
  OnDestroy
} from '@angular/core';
import * as d3 from 'd3';

import { event as d3Event } from 'd3-selection';
import { drag as d3Drag } from 'd3-drag';
import { select as d3Select } from 'd3-selection';

import { DraggableElementPosition } from './graph-editor.models';

const selectedDraggableElements = {};

@Directive({ selector: '[draggable-element]' })
export class DraggableElementDirective implements AfterContentInit, OnInit, OnDestroy {

  /** Set the draggable element position */
  @Input() position: DraggableElementPosition;
  /** Set readonly mode */
  @Input() readonlyMode: boolean;
  /** Draggable group name for multidrag */
  @Input() draggableGroupName = 'global';
  /** Set element as selected */
  @Input() get selected() {
    return this._selected;
  }
  set selected(value: boolean) {
    if (this._draggableGroup) {
      if (value) {
        this._draggableGroup.set(this, this);
      } else {
        this._draggableGroup.delete(this);
      }
    }

    this._selected = value;
  }

  @Output() positionChange = new EventEmitter<DraggableElementPosition>();
  @Output() onClickEvent = new EventEmitter();
  @Output() onDoubleClickEvent = new EventEmitter();
  @Output() setEditorDirty = new EventEmitter();

  private _clicks = 0;
  private _element: d3.Selection<any>;
  private _lastUpdateCall: number;

  private _selected: boolean;
  private _draggableGroup: Map<any, any>;

  constructor(private elementRef: ElementRef<SVGElement>, private _ngZone: NgZone) {
    this._element = d3Select(this.elementRef.nativeElement);
    this._setPosition = this._setPosition.bind(this);
  }

  /** lifecycle methods */
  ngOnInit(): void {
    if (!selectedDraggableElements[this.draggableGroupName]) {
      selectedDraggableElements[this.draggableGroupName] = new Map();
    }
    this._draggableGroup = selectedDraggableElements[this.draggableGroupName];
    if (this._selected) {
      this._draggableGroup.set(this, this);
    } else {
      this._draggableGroup.delete(this);
    }
    this._setPosition();
  }

  ngAfterContentInit() {
    this._element.on('click', this._onClick.bind(this));
    if (!this.readonlyMode) {
      this._element.call(d3Drag()
        .on('drag', this._onDrag.bind(this))
        .on('start', () => {
          d3Event.sourceEvent.stopPropagation();
          this.setEditorDirty.emit();
          document.body.classList.add('dragging');
        }).on('end', () => document.body.classList.remove('dragging')));
    }
  }

  ngOnDestroy(): void {
    this._draggableGroup.delete(this);
  }
  /** lifecycle methods */

  private _onDrag(e) {
    const event = d3Event;
    event.sourceEvent.preventDefault();
    if (this.selected) {
      this._draggableGroup.forEach(ref => {
        ref._dragmove(event);
      });
     } else {
      this._dragmove(event);
    }
  }

  private _dragmove(event) {
    this.position = {
      x: this.position.x + event.dx,
      y: this.position.y + event.dy
    };
    if (this._lastUpdateCall) {
      cancelAnimationFrame(this._lastUpdateCall);
      this._lastUpdateCall = null;
    }
    this.positionChange.emit(this.position);
    this._lastUpdateCall = requestAnimationFrame(this._setPosition);
  }

  private _setPosition() {
    const value = `translate(${this.position.x},${this.position.y})`;
    this._element.attr('transform', value);
  }

  private _onClick() {
    d3Event.stopPropagation();
    this._clicks++;
    if (this._clicks === 1) {
      this.onClickEvent.emit();
      setTimeout(() => {
        if (this._clicks !== 1) {
          this.onDoubleClickEvent.emit();
        }
        this._clicks = 0;
      }, 200);
    }

  }
}
