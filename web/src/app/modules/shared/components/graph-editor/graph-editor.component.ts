/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  NgZone,
  OnInit,
  Output,
  OnDestroy,
} from '@angular/core';
import * as d3 from 'd3';
import { event as d3Event } from 'd3-selection';
import { zoom as d3Zoom } from 'd3-zoom';
import { select as d3Select } from 'd3-selection';
import { zoomIdentity } from 'd3-zoom';
import { cloneDeep as _cloneDeep } from 'lodash';
import { WizardNode, WizardEdge } from '@app/wizard/models/node';
import { ZoomTransform, CreationData, NodeConnector, DrawingConnectorStatus } from '@app/wizard/models/drag';

@Component({
  selector: 'graph-editor',
  styleUrls: ['graph-editor.component.scss'],
  templateUrl: 'graph-editor.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class GraphEditorComponent implements OnDestroy, OnInit {

  @Input() workflowID = '';
  @Input() workflowEdges: Array<WizardEdge> = [];
  @Input()
  get editorPosition(): ZoomTransform {
    return this._editorPosition;
  }
  set editorPosition(value) {
    this._editorPosition = _cloneDeep(value);
    if (this._SVGParent) {
      this._externalPosition = true;
      this._setCurrentPosition();
    }
  }
  @Input() workflowNodes: Array<WizardNode> = [];
  @Input() creationMode: CreationData;
  @Input() selectedNodeName = '';
  @Input() selectedEdge: WizardEdge;
  @Input() serverStepValidations: any = {};
  @Input() multiselectionEnabled = true;

  @Input() get connectorPosition() {
    return this._connectorPosition;
  }
  set connectorPosition(position: ZoomTransform) {
    this._connectorPosition = position;
    if (position) {
      this.drawConnector(position);
    }
  }

  @Input() maxScale = 4;
  @Input() minScale = 0.1;
  @Input() disableEvents = false;

  @Output() createNode = new EventEmitter<any>();
  @Output() setEditorDirty = new EventEmitter();
  @Output() onClickEditor = new EventEmitter<{x: number; y: number}>();
  @Output() removeConnector = new EventEmitter();
  @Output() editorPositionChange = new EventEmitter<ZoomTransform>();
  @Output() finishSelection = new EventEmitter<any>();


  get enableDrag() {
    return this._enableDrag;
  }
  set enableDrag(value) {
    this._enableDrag = value;
    this._SVGParent.classed('drag-enabled', value);
    if (!value) {
      this._SVGParent.on('mousedown.zoom', null);
    } else {
      this.setDraggableEditor();
      this._setCurrentPosition();
    }
  }

  public eventTransform: ZoomTransform = { x: 0, y: 0, k: 1 };
  public drawingConnectionStatus: DrawingConnectorStatus = {
    status: false,
    name: ''
  };
  public _editorPosition: ZoomTransform;
  public initialSelectionCoors: any;


  /** Selectors */
  private _SVGParent: d3.Selection<any>;
  private _SVGContainer: d3.Selection<any>;
  private _documentRef: d3.Selection<any>;
  private _connectorElement: d3.Selection<any>;
  private _connectorPosition: ZoomTransform = null;
  private _enableDrag: boolean;
  /** when external position is true, it prevents sending a setEditorDirty event */
  private _externalPosition = true;
  private _isShiftPressed = false;

  private zoom: any;

  constructor(
    private _elementRef: ElementRef,
    private _cd: ChangeDetectorRef,
    private _ngZone: NgZone
  ) {
    this._cd.detach();
  }

  ngOnInit(): void {
    this._initSelectors();
    // Set initial position
    this.setDraggableEditor();
    this._setCurrentPosition();
    this.enableDrag = false;
    this._onBlur = this._onBlur.bind(this);
    window.addEventListener('blur', this._onBlur);
  }

  ngOnDestroy(): void {
    window.removeEventListener('blur', this._onBlur);
  }

  private _initSelectors() {
    this._documentRef = d3Select(document);
    const element: d3.Selection<any> = d3Select(this._elementRef.nativeElement);
    this._SVGParent = element.select('.composition');
    this._SVGContainer = this._SVGParent.select('.svg-container');
    this._connectorElement = element.select('.connector-line');
  }

  private _onBlur() {
    this._isShiftPressed = false;
    this.enableDrag = false;
  }

  @HostListener('document:keydown', ['$event'])
  onKeydownHandler(event: KeyboardEvent) {
    if (!this.disableEvents) {
      switch (event.keyCode) {
        /** PLUS */
        case 107: {
          this.changeZoom(this._editorPosition.k * 1.2);
          break;
        }
        /** MINUS */
        case 109: {
          this.changeZoom(this._editorPosition.k * 0.8);
          break;
        }
        /** SHIFT */
        case 16: {
          this._isShiftPressed = true;
          break;
        }
        /** SPACE and ALT*/
        case 32: case 18: {
          if (!this.enableDrag) {
            this.enableDrag = true;
          }
          break;
        }
      }
    }
  }

  @HostListener('document:keyup', ['$event'])
  onKeyupHandler(event: KeyboardEvent) {
    switch (event.keyCode) {
      /** 1 */
      case 49: {
        if (this._isShiftPressed) {
          this.centerWorkflow();
        }
        break;
      }
      /** 0 */
      case 48: {
        if (this._isShiftPressed) {
          this.changeZoom(1);
        }
        break;
      }
      /** SHIFT */
      case 16: {
        this._isShiftPressed = false;
        break;
      }
      /** SPACE and ALT*/
      case 32: case 18: {
        this.enableDrag = false;
        break;
      }
    }
  }

  clickDetected($event: any) {
    const position = {
      x: ($event.offsetX - this._editorPosition.x) / this._editorPosition.k,
      y: ($event.offsetY - this._editorPosition.y) / this._editorPosition.k
    };
    if (this.creationMode && this.creationMode.active) {
      this.createNode.emit({ position, creationMode: this.creationMode });
    }
    this.onClickEditor.emit(position);
  }

  drawConnector(startPosition: ZoomTransform) {
    const editorOffset: ClientRect = (<Element>this._SVGParent.node()).getBoundingClientRect();
    const connector: NodeConnector = {
      x1: startPosition.x,
      y1: startPosition.y - editorOffset.top,
      x2: 0,
      y2: 0
    };
    this._connectorElement.attr('d', ''); // reset connector position
    this._connectorElement.classed('show', true);
    const w = this._documentRef
      .on('mousemove', drawConnector.bind(this))
      .on('mouseup', mouseup.bind(this));

    function mouseup() {
      this._connectorElement.classed('show', false);
      w.on('mousemove', null).on('mouseup', null);
      this._cd.markForCheck();
      this.removeConnector.emit();
    }

    function drawConnector() {
      connector.x2 = d3Event.clientX - connector.x1;
      connector.y2 = d3Event.clientY - connector.y1 - editorOffset.top;
      this._connectorElement.attr('d', 'M ' + connector.x1 + ' ' + connector.y1 + ' l ' + connector.x2 + ' ' + connector.y2);
    }
  }

  changeZoom(k: number) {
    if (k < this.minScale) {
      k = this.minScale;
    } else if (k > this.maxScale) {
      k = this.maxScale;
    }
    this._editorPosition = {
      x: this._editorPosition.x,
      y: this._editorPosition.y,
      k
    };
    this._setCurrentPosition();
    this._ngZone.run(() => {
      this.editorPositionChange.emit(this._editorPosition);
    });
  }

  centerWorkflow(): void {
    const container = (<Element>this._SVGContainer.node()).getBoundingClientRect();
    const SVGParent = (<Element>this._SVGParent.node()).getBoundingClientRect();
    const containerWidth = container.width;
    const containerHeight = container.height;
    const svgWidth = SVGParent.width;
    const svgHeight = SVGParent.height;
    const translateX = Math.round(((svgWidth - containerWidth) / 2 - container.left) / this._editorPosition.k);
    const translateY = Math.round(((svgHeight - containerHeight) / 2 - container.top) / this._editorPosition.k);
    this._SVGParent.call(this.zoom.translateBy, translateX, Math.round(translateY + 135 / this._editorPosition.k));
  }

  setDraggableEditor() {
    this._ngZone.runOutsideAngular(() => {
      /** zoom behaviour */
      this.zoom = d3Zoom()
        .scaleExtent([this.minScale, this.maxScale])
        .wheelDelta(this._deltaFn)
        .on('start', () => {
          if (this._externalPosition) {
            this._externalPosition = false;
          } else {
            this.setEditorDirty.emit();
          }
        })
        .on('zoom', this._zoomed.bind(this));
      // Apply Zoom behaviour on parent
      this._SVGParent.call(this.zoom)
        .on('contextmenu', () => d3Event.preventDefault()) // disable right click
        .on('dblclick.zoom', null); // disable default double click effect
    });
  }

  createSelector(event) {
    if (event.button !== 0) {
      return;
    }
    if (this.enableDrag) {
      this.initialSelectionCoors = null;
    } else if (this.multiselectionEnabled) {
      this.initialSelectionCoors = {
        x: event.offsetX,
        y: event.offsetY
      };
      this._cd.reattach();

    }
  }

  onFinishSelection(event) {
    this.finishSelection.emit(event);
    this.initialSelectionCoors = null;
    setTimeout(() => this._cd.detach());
  }

  /** wheel delta  */
  private _deltaFn() {
    return -d3Event.deltaY * (d3Event.deltaMode ? 0.0387 : 0.002258);
  }

  /** Update element scale and position */
  private _zoomed(): void {
    const oldK = this._editorPosition.k;
    this._editorPosition = d3Event.transform;
    if (this._editorPosition.k !== oldK) {
      this._ngZone.run(() => {
        this.editorPositionChange.emit(this._editorPosition);
      });
    }
    this._SVGContainer.attr('transform', d3Event.transform);
  }

  private _setCurrentPosition() {
    this._SVGParent.call(this.zoom.transform, zoomIdentity.translate(this._editorPosition.x, this._editorPosition.y)
      .scale(this._editorPosition.k === 0 ? 1 : this._editorPosition.k));
  }
}
