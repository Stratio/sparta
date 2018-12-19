/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  AfterContentInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef, EventEmitter,
  Input,
  NgZone,
  OnChanges, Output,
  SimpleChanges,
  ViewEncapsulation
} from '@angular/core';

import { event as d3Event } from 'd3-selection';
import { select as d3Select } from 'd3-selection';
import { WizardEdgeModel } from './wizard-edge.model';

import { ENTITY_BOX } from './../../wizard.constants';
import { WizardNodePosition } from './../../models/node';

@Component({
  selector: '[wizard-edge]',
  styleUrls: ['wizard-edge.styles.scss'],
  templateUrl: 'wizard-edge.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})
export class WizardEdgeComponent implements AfterContentInit, OnChanges {


  @Input() edge: WizardEdgeModel;
  @Input() selectedEdge: any;
  @Input() workflowID = '';
  @Input() initPosition: WizardNodePosition;
  @Input() endPosition: WizardNodePosition;

  @Output() selectEdge = new EventEmitter<any>();
  @Output() showEdgeOptions = new EventEmitter<any>();

  public isSelected = false;

  private h = ENTITY_BOX.height;
  private w = ENTITY_BOX.width;
  private _dataType: string;

  private _svgAuxDefs: any;
  private _edgeElement: any;

  private _initialEntityName: string;
  private _finalEntityName: string;
  private _supportedDataRelations: Array<string>;

  private _internalUUID: string;

  constructor(private elementRef: ElementRef,
    private _cd: ChangeDetectorRef,
    private _ngZone: NgZone) {
    this._cd.detach();
    this._internalUUID = this._newGuid();
  }

  ngAfterContentInit(): void {
    this._initialEntityName = this.edge.origin.name;
    this._finalEntityName = this.edge.destination.name;
    this._supportedDataRelations = this.edge.origin.supportedDataRelations;
    this._dataType = this.edge.dataType;
    const container = d3Select(this.elementRef.nativeElement.querySelector('.sparta-edge-container'));

    this._svgAuxDefs = container.append('defs')
      .append('path')
      .attr('id', 'path' + this._internalUUID);

    this._edgeElement = container.append('use')
      .attr('xlink:xlink:href', '#path' + this._internalUUID)
      .attr('class', 'edge')
      .classed('special', this._dataType && this._dataType !== 'ValidData')
      .attr('marker-start', 'url(#UpArrowPrecise)')
      .attr('marker-end', 'url(#DownArrowPrecise)');

    const auxEdge = container.append('use')
      .attr('xlink:xlink:href', '#path' + this._internalUUID)
      .attr('class', 'edge-hover')
      .attr('stroke-width', '15')
      .on('contextmenu', (d, i) => {
        const event = d3Event;
        event.preventDefault();
        this.selectEdge.emit({
          origin: this._initialEntityName,
          destination: this._finalEntityName
        });
        this.showEdgeOptions.emit({
          clientX: event.clientX,
          clientY: event.clientY,
          relation: {
            initialEntityName: this._initialEntityName,
            finalEntityName: this._finalEntityName
          },
          edgeType: this._dataType,
          supportedDataRelations: this._supportedDataRelations
        });
      });
    auxEdge.on('click', this.selectedge.bind(this));
    this.getPosition(this.initPosition.x, this.initPosition.y, this.endPosition.x, this.endPosition.y);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this._ngZone.runOutsideAngular(() => {
      if (this._edgeElement && changes.selectedEdge) {
        this._edgeElement.classed('selected', this.selectedEdge && this.selectedEdge.origin === this._initialEntityName
          && this.selectedEdge.destination === this._finalEntityName ? true : false);
      }
      if (changes.initPosition || changes.endPosition) {
        this.getPosition(this.initPosition.x, this.initPosition.y, this.endPosition.x, this.endPosition.y);
        return;
      }
      if (changes.edge && this._edgeElement) {
        this._dataType = this.edge.dataType;
        this._edgeElement.classed('special', this._dataType && this._dataType !== 'ValidData');
      }
    });
  }

  getPosition(x1: number, y1: number, x2: number, y2: number) {
    if (!this._svgAuxDefs) {
      return;
    }
    const diff = Math.abs(x1 - x2);
    if (diff > this.w + 16) {
      y1 += this.h / 2;
      y2 += this.h / 2;
      if (x1 > x2) {
        x2 += this.w;
      } else {
        x1 += this.w;
      }
      this._svgAuxDefs.attr('d', 'M' + x2 + ',' + y2 + ' C' + x1 + ',' + y2 + ' ' + x2 + ',' + y1 + ' ' + x1 + ',' + y1);
    } else {
      x1 += this.w / 2;
      x2 += this.w / 2;
      if (y1 > y2) {
        y2 += this.h;
      } else {
        y1 += this.h;
      }
      this._svgAuxDefs.attr('d', 'M' + x2 + ',' + y2 + ' C' + x2 + ',' + y1 + ' ' + x1 + ',' + y2 + ' ' + x1 + ',' + y1);
    }
  }

  selectedge() {
    // stop graph-editor click detection
    d3Event.stopPropagation();

    this.selectEdge.emit({
      origin: this._initialEntityName,
      destination: this._finalEntityName
    });
  }

  private _newGuid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      const r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }
}
