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
import { getEdgePosition, getBezierEdge } from '@app/shared/wizard/utils/edge.utils';

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
  @Input() activedEdge: Boolean;
  @Input() qualityRule: number;
  @Input() qualityStatus: boolean;

  @Output() selectEdge = new EventEmitter<any>();
  @Output() showEdgeOptions = new EventEmitter<any>();
  @Output() showQualityRuleDetail = new EventEmitter<any>();

  public isSelected = false;

  private h = ENTITY_BOX.height;
  private w = ENTITY_BOX.width;
  private _dataType: string;

  private _container: any;
  private _svgAuxDefs: any;
  private _edgeElement: any;
  private _edgeLabel: any;

  private _supportedDataRelations: Array<string>;

  private _internalUUID: string;

  constructor(private elementRef: ElementRef,
    private _cd: ChangeDetectorRef,
    private _ngZone: NgZone) {
    this._cd.detach();
    this._internalUUID = this._newGuid();
  }

  ngAfterContentInit(): void {
    this._supportedDataRelations = this.edge.origin.supportedDataRelations;
    this._dataType = this.edge.dataType;
    this._container = d3Select(this.elementRef.nativeElement.querySelector('.sparta-edge-container'));

    this._svgAuxDefs = this._container.append('defs')
      .append('path')
      .attr('id', 'path' + this._internalUUID);

    this._edgeElement = this._container.append('use')
      .attr('xlink:xlink:href', '#path' + this._internalUUID)
      .attr('class', 'edge')
      .classed('special', this._dataType && this._dataType !== 'ValidData')
      .attr('marker-end', 'url(#UpArrowPrecise)')
      .attr('marker-start', 'url(#DownArrowPrecise)');


    const auxEdge = this._container.append('use')
      .attr('xlink:xlink:href', '#path' + this._internalUUID)
      .attr('class', 'edge-hover')
      .attr('stroke-width', '15')
      .on('contextmenu', (d, i) => {
        const event = d3Event;
        event.preventDefault();
        this.selectEdge.emit({
          origin: this.edge.origin.name,
          destination: this.edge.destination.name
        });
        this.showEdgeOptions.emit({
          clientX: event.clientX,
          clientY: event.clientY,
          relation: {
            initialEntityName: this.edge.origin.name,
            finalEntityName: this.edge.destination.name
          },
          edgeType: this._dataType,
          supportedDataRelations: this._supportedDataRelations
        });
      });
    auxEdge.on('click', this.selectedge.bind(this));

    if (this.qualityRule) {
      this._paintQRLabel(this.qualityStatus);
    }
    this.getPosition(this.initPosition.x, this.initPosition.y, this.endPosition.x, this.endPosition.y);

  }

  ngOnChanges(changes: SimpleChanges): void {
    this._ngZone.runOutsideAngular(() => {

      if (this._edgeElement && changes.selectedEdge) {
        this._edgeElement.classed('selected', this.selectedEdge && this.selectedEdge.origin === this.edge.origin.name
          && this.selectedEdge.destination === this.edge.destination.name ? true : false);
      }
      if (changes.initPosition || changes.endPosition) {
        this.getPosition(this.initPosition.x, this.initPosition.y, this.endPosition.x, this.endPosition.y);
        return;
      }
      if (changes.edge && this._edgeElement) {
        this._dataType = this.edge.dataType;
        this._edgeElement.classed('special', this._dataType && this._dataType !== 'ValidData');
      }

      if (!this._edgeLabel && changes.qualityRule) {
        this._paintQRLabel(this.qualityStatus);
        this.getPosition(this.initPosition.x, this.initPosition.y, this.endPosition.x, this.endPosition.y);
      }
      this._edgeElement.classed('actived', this.activedEdge);
    });
  }

  getPosition(x1: number, y1: number, x2: number, y2: number) {
    if (!this._svgAuxDefs) {
      return;
    }
    const coors = getBezierEdge(this._svgAuxDefs, x1, y1, x2, y2, this.h, this.w);
    if (this._edgeLabel) {
      this._edgeLabel.attr('transform', `translate(${(coors.x1 - (coors.x1 - coors.x2) / 2)}, ${(coors.y1 - (coors.y1 - coors.y2) / 2)})`);
    }
  }

  selectedge() {
    // stop graph-editor click detection
    d3Event.stopPropagation();
    this.selectEdge.emit({
      origin: this.edge.origin.name,
      destination: this.edge.destination.name
    });
  }

  private _newGuid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      const r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  private _paintQRLabel(status) {
    this._edgeLabel = this._container.append('g')
    .attr('class', 'edge__label-container');

    this._edgeLabel.on('click', () => {
      this.showQualityRuleDetail.emit();
    });

    this._edgeLabel.append('circle')
      .attr('fill', 'white')
      .attr('r', 18)
      .attr('stroke', '#eaeff5')
      .attr('stroke-width', '2');

    this._edgeLabel.append('text')
      .text('QR')
      .attr('y', -2)
      .attr('class', 'edge__label-text');

    this._edgeLabel.append('text')
      .text(this.qualityRule)
      .attr('y', 12)
      .attr('class', 'edge__label-text');

    this._edgeLabel.append('circle')
      .attr('r', 4)
      .attr('cy', -15)
      .attr('cx', 15)
      .attr('fill', status ? '#2cce93' : '#ec445c');
  }
}
