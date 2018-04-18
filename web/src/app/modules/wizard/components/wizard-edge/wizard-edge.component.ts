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
   ElementRef,
   Input,
   OnChanges,
   SimpleChanges,
   ViewEncapsulation
} from '@angular/core';
import { Store } from '@ngrx/store';
import * as d3 from 'd3';

import * as fromWizard from './../../reducers';
import * as wizardActions from './../../actions/wizard';
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

   @Input() initialEntityName: string;
   @Input() finalEntityName: string;
   @Input() selectedEdge: any;
   @Input() index = 0;
   @Input() initPosition: WizardNodePosition;
   @Input() endPosition: WizardNodePosition;

   public edge = '';
   public isSelected = false;

   private h = ENTITY_BOX.height;
   private w = ENTITY_BOX.width;

   private _svgAuxDefs: any;
   private _edgeElement: any;

   constructor(private elementRef: ElementRef, private store: Store<fromWizard.State>, private _cd: ChangeDetectorRef) {
      this._cd.detach();
   }

   ngAfterContentInit(): void {
      const container = d3
         .select(this.elementRef.nativeElement.querySelector('.sparta-edge-container'));

      this._svgAuxDefs = container.append('defs')
         .append('path')
         .attr('id', 'path' + this.index);

      this._edgeElement = container.append('use')
         .attr('xlink:xlink:href', '#path' + this.index)
         .attr('class', 'edge')
         .attr('marker-start', 'url(#UpArrowPrecise)')
         .attr('marker-end', 'url(#DownArrowPrecise');

      const auxEdge = container.append('use')
         .attr('xlink:xlink:href', '#path' + this.index)
         .attr('class', 'edge-hover')
         .attr('stroke-width', '15');

      auxEdge.on('click', this.selectedge.bind(this));
      this.getPosition(this.initPosition.x, this.initPosition.y, this.endPosition.x, this.endPosition.y);
   }

   ngOnChanges(changes: SimpleChanges): void {
      if (!changes.initPosition && !changes.endPosition) {
         if (changes.selectedEdge) {
            if (this.selectedEdge && this.selectedEdge.origin === this.initialEntityName
               && this.selectedEdge.destination === this.finalEntityName) {
               this._edgeElement.attr('class', 'edge selected');
            } else {
               this._edgeElement.attr('class', 'edge');
            }
         }
      } else {
         this.getPosition(this.initPosition.x, this.initPosition.y, this.endPosition.x, this.endPosition.y);
      }
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
            x1 += 8;
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
      d3.event.stopPropagation();
      this.store.dispatch(new wizardActions.UnselectEntityAction());
      if (this.selectedEdge && this.selectedEdge.origin === this.initialEntityName
         && this.selectedEdge.destination === this.finalEntityName) {
         this.store.dispatch(new wizardActions.UnselectSegmentAction());
      } else {
         this.store.dispatch(new wizardActions.SelectSegmentAction({
            origin: this.initialEntityName,
            destination: this.finalEntityName
         }));
      }
   }
}
