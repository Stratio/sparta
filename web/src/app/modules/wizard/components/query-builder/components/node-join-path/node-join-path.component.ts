/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, Input, ChangeDetectorRef, AfterContentInit, ElementRef, OnChanges, SimpleChanges } from '@angular/core';
import { select as d3Select } from 'd3-selection';
import { INPUT_SCHEMAS_MAX_HEIGHT } from '@app/wizard/components/query-builder/query-builder.constants';

@Component({
  selector: '[node-join-path]',
  template: '<svg:g class="sparta-join-path-container"></svg:g>',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class NodeJoinPathComponent implements AfterContentInit, OnChanges {

  @Input() pathNumber: number;
  @Input() totalPath: number;
  @Input() get pathData() {
    return this._pathData;
  }
  set pathData(value) {
    this._pathData = value;

  }
  @Input() selectedFieldNames;
  @Input() containerPosition: any;
  @Input() containerPosition2: any;

  public coordinates: any;
  private _pathData: any;
  private _path: any;
  private _active = false;

  constructor(private _cd: ChangeDetectorRef,
    private elementRef: ElementRef) {
    this._cd.detach();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.selectedFieldNames) {
      const selectedTableFields = this.selectedFieldNames[this.pathData.initData.tableName];
      this._active = selectedTableFields && !!selectedTableFields.length;
      if (this._path && !this._pathData.lostField) {
        this._path
          .attr('stroke', this._active ? '#37b5e4' : '#aab7c4')
          .attr('marker-end', this._active ? 'url(#arrow-schemas-active)' : 'url(#arrow-schemas)');
      }
    }
  }

  ngAfterContentInit(): void {
    const pathData = this._getPath(this._pathData.coordinates);

    const container = d3Select(this.elementRef.nativeElement.querySelector('.sparta-join-path-container'));
    this._path = container.append('path')
      .attr('stroke', this._pathData.lostField ? '#ec445c' : (this._active ? '#37b5e4' : '#aab7c4'))
      .attr('fill', 'none')
      .attr('d', pathData)
      .attr('marker-start', this._pathData.lostField  ? 'url(#box-connection-error)' : 'url(#box-connection)')
      .attr('marker-end', this._pathData.lostField ? 'url(#arrow-schemas-error)' : (this._active ? 'url(#arrow-schemas-active)' : 'url(#arrow-schemas)'));
  }

  private _getPath(position: any) {
    const coordinates = {
      x1: position.init.x,
      y1: position.init.y + (position.init.height / 2),
      x2: position.end.x - 2, // 2px border-width
      y2: position.end.y + (position.end.height / 2)
    };

    if (coordinates.y1 < this.containerPosition.y) {
      coordinates.y1 = this.containerPosition.y;
    }
    if (this.containerPosition.y + INPUT_SCHEMAS_MAX_HEIGHT < coordinates.y1) {
      coordinates.y1 = this.containerPosition.y + INPUT_SCHEMAS_MAX_HEIGHT;
    }
    if (coordinates.y2 < this.containerPosition2.y) {
      coordinates.y2 = this.containerPosition2.y;
    }
    if (this.containerPosition2.y + INPUT_SCHEMAS_MAX_HEIGHT < coordinates.y2) {
      coordinates.y2 = this.containerPosition2.y + INPUT_SCHEMAS_MAX_HEIGHT;
    }

    const xt = coordinates.x1 - 40 - this.pathNumber * 30;
    const rad = 14;
    const inverse = (coordinates.y1 - coordinates.y2) > 0;

    return `M${coordinates.x1},${coordinates.y1} L${xt + rad},${coordinates.y1} ` +
      `A ${rad},${rad} 0 0 ${inverse ? 1 : 0} ${xt}, ${coordinates.y1 + (inverse ? -rad : rad)}` +
      `L${xt},${coordinates.y2 - (inverse ? -rad : rad)}` +
      `A ${rad},${rad} 0 0 ${inverse ? 1 : 0} ${xt + rad},${coordinates.y2} ` +
      `L${coordinates.x2},${coordinates.y2}`;

  }
}


