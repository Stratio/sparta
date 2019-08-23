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
  OnInit,
  Output,
  OnDestroy,
  Renderer2
} from '@angular/core';

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
        this._draggableGroup.set(this._draggableSymbol, this);
      } else {
        this._draggableGroup.delete(this._draggableSymbol);
      }
    }
    this._selected = value;
  }

  @Output() positionChange = new EventEmitter<DraggableElementPosition>();
  @Output() onFinishPositionChange = new EventEmitter<DraggableElementPosition>();
  @Output() onClickEvent = new EventEmitter<any>();
  @Output() onDoubleClickEvent = new EventEmitter<any>();
  @Output() setEditorDirty = new EventEmitter<void>();

  private _clicks = 0;
  private _element: any;
  private _nativeElement: SVGElement;

  private _selected: boolean;
  private _draggableGroup: Map<Symbol, DraggableElementDirective>;
  /* Key of the map instance */
  private _draggableSymbol = Symbol();
  private _initialPosition: { x: number, y: number };

  constructor(_elementRef: ElementRef<SVGElement>,
    private _renderer: Renderer2) {
    this._nativeElement = _elementRef.nativeElement;
    this._element = d3Select(_elementRef.nativeElement);
    this._setPosition = this._setPosition.bind(this);
  }

  /** lifecycle methods */
  ngOnInit(): void {
    if (!selectedDraggableElements[this.draggableGroupName]) {
      selectedDraggableElements[this.draggableGroupName] = new Map();
    }
    this._draggableGroup = selectedDraggableElements[this.draggableGroupName];
    if (this._selected) {
      this._draggableGroup.set(this._draggableSymbol, this);
    } else {
      this._draggableGroup.delete(this._draggableSymbol);
    }
    this._setPosition(this.position);
  }

  ngAfterContentInit() {
    this._element.on('click', this._onClick.bind(this));
    let firstDrag = true;
    if (!this.readonlyMode) {
      this._element.call(d3Drag()
        .on('start', () => {
          d3Event.sourceEvent.stopPropagation();
          document.body.classList.add('dragging');
          this._initialPosition = this.position;
        })
        .on('drag', () => {
          if (firstDrag) {
            this.setEditorDirty.emit();
            firstDrag = false;
          }
          this._onDrag.call(this);
        })
        .on('end', () => {
          if (this.position !== this._initialPosition) {
            this.onFinishPositionChange.emit(this.position);
          }
          document.body.classList.remove('dragging');
          firstDrag = true;
        }));
    }
  }

  ngOnDestroy(): void {
    this._draggableGroup.delete(this._draggableSymbol);
  }
  /** end lifecycle methods */

  private _onDrag() {
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

  private _dragmove(event): void {
    this.position = {
      x: this.position.x + event.dx,
      y: this.position.y + event.dy
    };
    const pos: DraggableElementPosition = this.position;
    this.positionChange.emit(pos);
    this._setPosition(pos);
  }

  private _setPosition(position: DraggableElementPosition): void {
    const value = `translate(${position.x},${position.y})`;
    this._renderer.setAttribute(this._nativeElement, 'transform', value);
  }

  private _onClick(): void {
    d3Event.stopPropagation();
    this._clicks++;
    if (this._clicks === 1) {
      this.onClickEvent.emit(d3Event);
      setTimeout(() => {
        if (this._clicks !== 1) {
          this.onDoubleClickEvent.emit(d3Event);
        }
        this._clicks = 0;
      }, 200);
    }
  }
}
