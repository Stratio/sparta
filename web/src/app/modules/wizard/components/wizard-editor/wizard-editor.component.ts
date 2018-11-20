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
import { event as d3Event } from 'd3-selection';
import { zoom as d3Zoom } from 'd3-zoom';
import { select as d3Select } from 'd3-selection';
import { zoomIdentity } from 'd3-zoom';
import { cloneDeep as _cloneDeep, isEqual as _isEqual } from 'lodash';
import { WizardEditorService } from './wizard-editor.sevice';
import { WizardNode, WizardEdge } from '@app/wizard/models/node';
import { ZoomTransform, CreationData, NodeConnector, DrawingConnectorStatus } from '@app/wizard/models/drag';

@Component({
  selector: 'wizard-editor',
  styleUrls: ['wizard-editor.styles.scss'],
  templateUrl: 'wizard-editor.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardEditorComponent implements OnInit {
  @Input() workflowType: string;
  @Input() workflowID = '';
  @Input() debugResult: any;
  @Input() workflowEdges: Array<WizardEdge> = [];
  @Input()
  get svgPosition(): ZoomTransform {
    return this._svgPosition;
  }
  set svgPosition(value) {
    this._svgPosition = _cloneDeep(value);
    if (this._SVGParent) {
      /** Update editor position */
      this._SVGParent.call(this.zoom.transform, zoomIdentity.translate(this._svgPosition.x, this._svgPosition.y)
        .scale(this._svgPosition.k === 0 ? 1 : this._svgPosition.k));
    }
  }
  @Input() workflowNodes: Array<WizardNode> = [];
  @Input() creationMode: CreationData;
  @Input() selectedNodeName = '';
  @Input() selectedEdge: WizardEdge;
  @Input() serverStepValidations: any = {};

  @Output() editEntity = new EventEmitter<WizardNode>();
  @Output() selectNode = new EventEmitter<WizardNode>();
  @Output() createNode = new EventEmitter<WizardNode>();
  @Output() onCreateEdge = new EventEmitter<WizardEdge>();
  @Output() setEditorDirty = new EventEmitter();
  @Output() deselectNode = new EventEmitter<void>();
  @Output() selectEdge = new EventEmitter<WizardEdge>();
  @Output() showEdgeOptions = new EventEmitter<any>();

  public showConnector = false;

  public eventTransform: ZoomTransform = { x: 0, y: 0, k: 1 };

  /** Selectors */
  private _SVGParent: d3.Selection<any>;
  private _SVGContainer: d3.Selection<any>;
  private _documentRef: d3.Selection<any>;
  private _connectorElement: d3.Selection<any>;

  public drawingConnectionStatus: DrawingConnectorStatus = {
    status: false,
    name: ''
  };

  public _svgPosition: ZoomTransform;
  private newOrigin = '';
  private zoom: any;

  constructor(
    private _elementRef: ElementRef,
    private _editorService: WizardEditorService,
    private _cd: ChangeDetectorRef,
    private _ngZone: NgZone
  ) {}

  ngOnInit(): void {
    this._initSelectors();
    this.setDraggableEditor();
    // Set initial position
    this._SVGParent.call(this.zoom.transform, zoomIdentity.translate(this._svgPosition.x, this._svgPosition.y)
      .scale(this._svgPosition.k === 0 ? 1 : this._svgPosition.k));
  }

  private _initSelectors() {
    this._documentRef = d3Select(document);
    const element: d3.Selection<any> = d3Select(this._elementRef.nativeElement);
    this._SVGParent = element.select('#composition');
    this._SVGContainer = element.select('#svg-container');
    this._connectorElement = element.select('.connector-line');
  }

  clickDetected($event: any) {
    if (this.creationMode.active) {
      let entity: any = {};
      const entityData = this.creationMode.data;
      if (entityData.type === 'copy') { // if its a copy, only sets the position
        entity = entityData.data;
      } else {
        entity = this._editorService.initializeEntity(this.workflowType, entityData, this.workflowNodes);
      }
      entity.uiConfiguration = {
        position: {
          x: ($event.offsetX - this._svgPosition.x) / this._svgPosition.k,
          y: ($event.offsetY - this._svgPosition.y) / this._svgPosition.k
        }
      };
      this.createNode.emit(entity);
    }
    this.deselectNode.emit();
  }

  createEdge(event: any) {
    this.drawConnector(event);
  }

  drawConnector(event: any) {
    const $event = event.event;
    this.newOrigin = event.name;
    const connector: NodeConnector = {
      x1: $event.clientX,
      y1: $event.clientY - 135,
      x2: 0,
      y2: 0
    };
    this.drawingConnectionStatus = {
      status: true,
      name: event.name
    };
    this._connectorElement.attr('d', ''); // reset connector position
    this.showConnector = true;
    const w = this._documentRef
      .on('mousemove', drawConnector.bind(this))
      .on('mouseup', mouseup.bind(this));

    function mouseup() {
      $event.target.parentNode.classList.remove('over2');
      this.showConnector = false;
      this.newOrigin = '';
      this.drawingConnectionStatus = {
        status: false
      };
      w.on('mousemove', null).on('mouseup', null);
      this._cd.markForCheck();
    }

    function drawConnector() {
      connector.x2 = d3Event.clientX - connector.x1;
      connector.y2 = d3Event.clientY - connector.y1 - 135;
      this._connectorElement.attr('d', 'M ' + connector.x1 + ' ' + connector.y1 + ' l ' + connector.x2 + ' ' + connector.y2);
    }
  }

  finishConnector(destinationEntity: any) {
    if (this.newOrigin && this.newOrigin.length) {
      this.onCreateEdge.emit({
        origin: this.newOrigin,
        destination: destinationEntity.name
      });
    }
  }

  changeZoom(zoomIn: boolean) {
    this._svgPosition.k += (zoomIn ? 0.2 : -0.2) * this._svgPosition.k;
    this.zoom.scaleTo(this._SVGParent, this._svgPosition.k);
  }

  centerWorkflow(): void {
    const container = this._elementRef.nativeElement.querySelector('#svg-container').getBoundingClientRect();
    const _SVGParent = this._elementRef.nativeElement.querySelector('#composition').getBoundingClientRect();
    const containerWidth = container.width;
    const containerHeight = container.height;
    const svgWidth = _SVGParent.width;
    const svgHeight = _SVGParent.height;
    const translateX = Math.round(((svgWidth - containerWidth) / 2 - container.left) / this._svgPosition.k);
    const translateY = Math.round(((svgHeight - containerHeight) / 2 - container.top) / this._svgPosition.k);
    this._SVGParent.call(this.zoom.translateBy, translateX, Math.round(translateY + 135 / this._svgPosition.k));
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
    this._svgPosition = d3Event.transform;
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
