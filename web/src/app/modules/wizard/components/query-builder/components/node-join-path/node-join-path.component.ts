/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, Input, ChangeDetectorRef, AfterContentInit, ElementRef, OnChanges, SimpleChanges } from '@angular/core';
import * as d3Shape from 'd3-shape';
import { select as d3Select } from 'd3-selection';
import { INPUT_SCHEMAS_MAX_HEIGHT } from '@app/wizard/components/query-builder/query-builder.constants';

@Component({
   selector: '[node-join-path]',
   styleUrls: ['node-join-path.component.scss'],
   templateUrl: 'node-join-path.component.html',
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


   public coordinates: any;
   private _pathData: any;
   private _path: any;
   private _lineGenerator: any;
   private _active = false;

   constructor(private _cd: ChangeDetectorRef,
      private elementRef: ElementRef) {
      this._cd.detach();
   }

   ngOnChanges(changes: SimpleChanges): void {
      if (changes.selectedFieldNames) {
       const selectedTableFields = this.selectedFieldNames[this.pathData.initData.tableName];
         this. _active = selectedTableFields && !!selectedTableFields.length;
         if (this._path) {
            this._path
               .attr('stroke', this. _active ? '#37b5e4' : '#aab7c4')
               .attr('marker-end', this._active ? 'url(#arrow-schemas-active)' : 'url(#arrow-schemas)');

         }
      }

   }

   ngAfterContentInit(): void {
      this._lineGenerator = d3Shape.line().curve(d3Shape.curveLinear);
      const points = this._getPoints(this._pathData.coordinates);
      const pathData = this._lineGenerator(points);

      const container = d3Select(this.elementRef.nativeElement.querySelector('.sparta-join-path-container'));
      this._path = container.append('path')
         .attr('stroke', this. _active ? '#37b5e4' : '#aab7c4')
         .attr('fill', 'none')
         .attr('d', pathData)
         .attr('marker-start', 'url(#box-connection)')
         .attr('marker-end', this._active ? 'url(#arrow-schemas-active)' : 'url(#arrow-schemas)');
   }

   _getPoints(position: any) {
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
      const points = [];
      let curveWidth = this.totalPath * 5;
      if (curveWidth > 0.6) {
         curveWidth = 0.6;
      }
      const portion = (curveWidth / this.totalPath) * (this.pathNumber);
      const operator = 0.6 - (curveWidth / 2) + portion;
      /** First coord (input origin) */
      points.push([coordinates.x1 - 2, coordinates.y1]);

      const xdiff = (coordinates.x2 - coordinates.x1) * operator;

      /** Second coord (middle point, same y coor than the input) */
      points.push([coordinates.x1 - 58 + position.offset, coordinates.y1]);
      /** Third coord (middle point, same y coor than the output)*/
      points.push([coordinates.x2 - 58 + position.offset, coordinates.y2]);
      /** Last coord (output) */
      points.push([coordinates.x2 - 2, coordinates.y2]);
      return points;
   }
}


