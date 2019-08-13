/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, Input, ChangeDetectorRef, AfterContentInit, ElementRef, OnChanges, SimpleChanges } from '@angular/core';
import { Selection } from 'd3';
import { select as d3Select } from 'd3-selection';
import { INPUT_SCHEMAS_MAX_HEIGHT } from '@app/wizard/components/query-builder/query-builder.constants';
import { Path } from '../../models/schema-fields';

@Component({
  selector: '[node-schema-path]',
  template: '<svg:g class="sparta-schema-path-container"></svg:g>',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class NodeSchemaPathComponent implements AfterContentInit, OnChanges {

  @Input() pathNumber: number;
  @Input() totalPath: number;
  @Input() containerPosition: any;
  @Input() get pathData(): Path {
    return this._pathData;
  }
  set pathData(value) {
    this._pathData = value;
    if (this._path) {
      const pathData = this._getPath(this._pathData.coordinates);
      this._path.attr('d', pathData);
    }
  }

  @Input() selectedFieldNames;

  public coordinates: any;
  private _pathData: Path;
  private _path: any;
  private _active = false;
  private _container: any;

  constructor(private _cd: ChangeDetectorRef,
    private elementRef: ElementRef) {
    this._cd.detach();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.selectedFieldNames) {
      const selectedTableFields = this.selectedFieldNames[this.pathData.initData.tableName];
      this._active = selectedTableFields && selectedTableFields.indexOf(this.pathData.initData.fieldName) > -1 ? true : false;
      if (this._path) {
        this._getPathStyles();
      }
    }
  }

  ngAfterContentInit(): void {
    this._container = d3Select(this.elementRef.nativeElement.querySelector('.sparta-schema-path-container'));
    this._getPathStyles();
  }

  _getPath(position: any) {
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
    let curveWidth = (this.totalPath - 1) * 0.3;
    if (curveWidth > 1.2) {
      curveWidth = 1.2;
    }
    const op = curveWidth / (this.totalPath === 1 ? 2 : this.totalPath - 1);
    const operator = -curveWidth / 2 + (op * this.pathNumber);
    const diff = (coordinates.x2 - coordinates.x1) / 2;
    const diffY = coordinates.y1 - coordinates.y2;
    const absDiffY = Math.abs(diffY);

    const inverse = diffY > 0;
    const xdiff = diff - (inverse ? -1 : 1) *  diff * operator;
    const xt = coordinates.x1 + xdiff;

    const rad =  absDiffY < 28 ? Math.floor( absDiffY / 2) : 14;
    return `M${coordinates.x1},${coordinates.y1} L${xt - rad},${coordinates.y1} ` +
      `A ${rad},${rad} 0 0 ${inverse ? 0 : 1} ${xt}, ${coordinates.y1 - (inverse ? rad : -rad)}` +
      `L${xt},${coordinates.y2 + (inverse ? rad : -rad)}` +
      `A ${rad},${rad} 0 0 ${inverse ? 1 : 0} ${xt + rad},${coordinates.y2} ` +
      `L${coordinates.x2},${coordinates.y2}`;
  }

  private _getPathStyles() {
    const pathData = this._getPath((this._pathData.coordinates));
    this._path = this._container.append('path')
      .attr('stroke', this._pathData.lostField ? '#ec445c' : (this._active ? '#37b5e4' : '#aab7c4'))
      .attr('fill', 'none')
      .attr('d', pathData)
      .attr('marker-start', this._pathData.lostField  ? 'url(#box-connection-error)' : 'url(#box-connection)')
      .attr('marker-end', this._pathData.lostField ? 'url(#arrow-schemas-error)' : (this._active ? 'url(#arrow-schemas-active)' : 'url(#arrow-schemas)'));
  }


}


