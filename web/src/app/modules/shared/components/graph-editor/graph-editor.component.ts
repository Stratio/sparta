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
  Input,
  NgZone,
  OnInit,
  Output,
} from '@angular/core';
import * as d3 from 'd3';
import { event as d3Event } from 'd3-selection';
import { zoom as d3Zoom } from 'd3-zoom';
import { select as d3Select } from 'd3-selection';
import { zoomIdentity } from 'd3-zoom';
import { cloneDeep as _cloneDeep, isEqual as _isEqual } from 'lodash';
import { WizardNode, WizardEdge } from '@app/wizard/models/node';
import { ZoomTransform, CreationData, NodeConnector, DrawingConnectorStatus } from '@app/wizard/models/drag';

@Component({
  selector: 'graph-editor',
  styleUrls: ['graph-editor.component.scss'],
  templateUrl: 'graph-editor.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class GraphEditorComponent implements OnInit {

  @Input() workflowID = '';
  @Input() workflowEdges: Array<WizardEdge> = [];
  @Input()
  get editorPosition(): ZoomTransform {
    return this._editorPosition;
  }
  set editorPosition(value) {
    this._editorPosition = _cloneDeep(value);
    if (this._SVGParent) {
      /** Update editor position */
      this._SVGParent.call(this.zoom.transform, zoomIdentity.translate(this._editorPosition.x, this._editorPosition.y)
        .scale(this._editorPosition.k === 0 ? 1 : this._editorPosition.k));
    }
  }
  @Input() workflowNodes: Array<WizardNode> = [];
  @Input() creationMode: CreationData;
  @Input() selectedNodeName = '';
  @Input() selectedEdge: WizardEdge;
  @Input() serverStepValidations: any = {};

  @Input() get connectorPosition() {
    return this._connectorPosition;
  }
  set connectorPosition(position: ZoomTransform) {
    this._connectorPosition = position;
    if (position) {
      this.drawConnector(position);
    }
  }

  @Output() createNode = new EventEmitter<any>();
  @Output() setEditorDirty = new EventEmitter();
  @Output() deselectNode = new EventEmitter<void>();
  @Output() removeConnector = new EventEmitter();

  public showConnector = false;

  public eventTransform: ZoomTransform = { x: 0, y: 0, k: 1 };

  /** Selectors */
  private _SVGParent: d3.Selection<any>;
  private _SVGContainer: d3.Selection<any>;
  private _documentRef: d3.Selection<any>;
  private _connectorElement: d3.Selection<any>;
  private _connectorPosition: ZoomTransform = null;

  public drawingConnectionStatus: DrawingConnectorStatus = {
    status: false,
    name: ''
  };

  public _editorPosition: ZoomTransform;
  private zoom: any;

  constructor(
    private _elementRef: ElementRef,
    private _cd: ChangeDetectorRef,
    private _ngZone: NgZone
  ) { }

  ngOnInit(): void {
    this._initSelectors();
    this.setDraggableEditor();
    // Set initial position
    this._SVGParent.call(this.zoom.transform, zoomIdentity.translate(this._editorPosition.x, this._editorPosition.y)
      .scale(this._editorPosition.k === 0 ? 1 : this._editorPosition.k));
  }

  private _initSelectors() {
    this._documentRef = d3Select(document);
    const element: d3.Selection<any> = d3Select(this._elementRef.nativeElement);
    this._SVGParent = element.select('.composition');
    this._SVGContainer = this._SVGParent.select('.svg-container');
    this._connectorElement = element.select('.connector-line');
  }

  clickDetected($event: any) {
    if (this.creationMode.active) {
      const position = {
        x: ($event.offsetX - this._editorPosition.x) / this._editorPosition.k,
        y: ($event.offsetY - this._editorPosition.y) / this._editorPosition.k
      };
      this.createNode.emit({position, creationMode: this.creationMode});
    }
    this.deselectNode.emit();
  }

  drawConnector(startPosition: ZoomTransform) {
    const connector: NodeConnector = {
      x1: startPosition.x,
      y1: startPosition.y,
      x2: 0,
      y2: 0
    };
    this._connectorElement.attr('d', ''); // reset connector position
    this.showConnector = true;
    const w = this._documentRef
      .on('mousemove', drawConnector.bind(this))
      .on('mouseup', mouseup.bind(this));

    function mouseup() {
      this.showConnector = false;
      w.on('mousemove', null).on('mouseup', null);
      this._cd.markForCheck();
      this.removeConnector.emit();
    }

    function drawConnector() {
      connector.x2 = d3Event.clientX - connector.x1;
      connector.y2 = d3Event.clientY - connector.y1 - 135;
      this._connectorElement.attr('d', 'M ' + connector.x1 + ' ' + connector.y1 + ' l ' + connector.x2 + ' ' + connector.y2);
    }
  }

  changeZoom(zoomIn: boolean) {
    this._editorPosition.k += (zoomIn ? 0.2 : -0.2) * this._editorPosition.k;
    this.zoom.scaleTo(this._SVGParent, this._editorPosition.k);
  }

  centerWorkflow(): void {
    const container = (<Element>this._SVGContainer.node()).getBoundingClientRect();
    const _SVGParent = (<Element>this._SVGParent.node()).getBoundingClientRect();
    const containerWidth = container.width;
    const containerHeight = container.height;
    const svgWidth = _SVGParent.width;
    const svgHeight = _SVGParent.height;
    const translateX = Math.round(((svgWidth - containerWidth) / 2 - container.left) / this._editorPosition.k);
    const translateY = Math.round(((svgHeight - containerHeight) / 2 - container.top) / this._editorPosition.k);
    this._SVGParent.call(this.zoom.translateBy, translateX, Math.round(translateY + 135 / this._editorPosition.k));
  }

  trackBySegmentFn(index: number, item: any) {
    return index; // or item.id
  }

  setDraggableEditor() {
    this._ngZone.runOutsideAngular(() => {
      /** zoom behaviour */
      this.zoom = d3Zoom()
        .scaleExtent([1 / 8, 4])
        .wheelDelta(this._deltaFn)
        .on('start', () => {
          const sourceEvent = d3Event.sourceEvent;
          if (sourceEvent) {
            sourceEvent.preventDefault();
            sourceEvent.stopPropagation();
          }
        })
        .on('zoom', this._zoomed.bind(this));
      // Apply Zoom behaviour on parent
      this._SVGParent.call(this.zoom)
        .on('contextmenu', () => d3Event.preventDefault()) // disable right click
        .on('dblclick.zoom', null); // disable default double click effect
    });
  }

  /** wheel delta  */
  private _deltaFn() {
    return -d3Event.deltaY * (d3Event.deltaMode ? 0.0387 : 0.002258);
  }

  /** Update element scale and position */
  private _zoomed(): void {
    this._editorPosition = d3Event.transform;
    this._SVGContainer.attr('transform', d3Event.transform);
    const sourceEvent = d3Event.sourceEvent;
    if (sourceEvent) {
      sourceEvent.preventDefault();
      sourceEvent.stopPropagation();
    }
    if ((sourceEvent && sourceEvent.type === 'mousemove') || !_isEqual(this.eventTransform, d3Event.transform)) {
      let emitChange = true;
      if (!sourceEvent) {
        const arrayZero = [1, 0, 0];
        let res1: any[] = null;
        let res2: any[] = null;
        if (this.eventTransform) {
          res1 = Object.keys(this.eventTransform).sort().map(key => {
            return this.eventTransform[key];
          });
        }
        if (d3Event.transform) {
          res2 = Object.keys(d3Event.transform).sort().map(key => {
            return d3Event.transform[key];
          });
        }
        if (_isEqual(res1, arrayZero) || _isEqual(res2, arrayZero)) {
          emitChange = false;
        }
      }
      if (emitChange) {
        this.setEditorDirty.emit();
      }
      this.eventTransform = _cloneDeep(d3Event.transform);
    }
  }
}
