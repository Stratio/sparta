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
   ViewEncapsulation
} from '@angular/core';
import { select as d3Select, event as d3Event } from 'd3-selection';

import { ENTITY_BOX } from './../../wizard.constants';
import { UtilsService } from '@app/shared/services/utils.service';
import { icons } from '@app/shared/constants/icons';
import { WizardNode } from '@app/wizard/models/node';
import { DrawingConnectorStatus } from '@app/wizard/models/drag';
import { StepType } from '@models/enums';


@Component({
   selector: '[wizard-node]',
   styleUrls: ['wizard-node.styles.scss'],
   templateUrl: 'wizard-node.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush,
   encapsulation: ViewEncapsulation.None
})
export class WizardNodeComponent implements OnInit {

   @Input() data: WizardNode;
   @Input() createdNew: boolean;


   @Input() get serverStepValidation(): string {
      return this._serverStepValidation;
   }
   set serverStepValidation(value: string) {
      this._serverStepValidation = value;
      if (this._errorIconElement) {
         this._getIcon(this._errorIconElement);
      }
   }


   @Input() get selected(): boolean {
      return this._selectedNode;
   }
   set selected(value: boolean) {
      if (this._selectedNode !== value && this._nodeRectElement) {
         this._nodeRectElement.classed('active', value);
      }
      this._selectedNode = value;
   }


   @Input() get drawingConnectionStatus() {
      return this._drawingConectionStatus;
   }
   set drawingConnectionStatus(dstatus: any) {
      if (this._containerElement) {
         this._containerElement.classed('creation-mode', dstatus.status && dstatus.name !== this.data.name);
      }
      this._drawingConectionStatus = dstatus;
   }


   @Input() get debugResult(): any {
      return this._debugResult;
   }
   set debugResult(value: any) {
      this._debugResult = value;
      if (this._errorIconElement) {
         this._getIcon(this._errorIconElement);
      }
   }


   @Output() onDrawConnector = new EventEmitter<any>();
   @Output() onFinishConnector = new EventEmitter<any>();

   /** Selectors */
   private _el: HTMLElement;
   private _containerElement: d3.Selection<any>;
   private _nodeRectElement: d3.Selection<any>;
   private _errorIconElement: d3.Selection<any>;

   private _drawingConectionStatus: DrawingConnectorStatus;
   private _selectedNode = false;
   private _debugResult: any;
   private _serverStepValidation: string;

   constructor(elementRef: ElementRef, private utilsService: UtilsService, private _cd: ChangeDetectorRef, private _ngZone: NgZone) {
      this._cd.detach();
      this._el = elementRef.nativeElement;
   }

   ngOnInit(): void {
      this._ngZone.runOutsideAngular(() => {
         const nodeElement = d3Select(this._el);
         this._containerElement = nodeElement.select('.sparta-node-box');
         if (this.createdNew) {
            this._containerElement.classed('created-new', true);
         }
         if (this.data.created) {
            this._containerElement.classed('current-created');
         }
         this.createAuxRect();
         this.createNodeRect();
         this.createNodeContent();
         this.generateEntries();
         nodeElement.on('mouseup', () => {
            if (this.data.stepType !== StepType.Input && this._drawingConectionStatus.status) {
               this.onFinishConnector.emit(this.data);
            }
         });
      });
   }

   createAuxRect() {
      this._containerElement.append('rect')
         .attr('x', -8)
         .attr('y', -8)
         .attr('fill', 'transparent')
         .attr('height', ENTITY_BOX.height + 16) // 16 = ENTITY_BOX.connectorsWidth + ENTITY_BOX.strokeWidth * 2
         .attr('width', ENTITY_BOX.width + 16);
   }

   createNodeRect() {
      this._nodeRectElement = this._containerElement.append('rect')
         .attr('fill', 'white')
         .attr('height', ENTITY_BOX.height)
         .attr('width', ENTITY_BOX.width)
         .attr('stroke-width', ENTITY_BOX.strokeWidth)
         .attr('rx', 6)
         .attr('ry', 6)
         .attr('stroke-linecap', 'round')
         .attr('class', 'entity ' + this.data.stepType.toLowerCase() + '-step');
      if (this._selectedNode) {
         this._nodeRectElement.classed('selected', true);
      }
   }

   createNodeContent() {
      const textContainer = this._containerElement.append('svg')
         .attr('width', ENTITY_BOX.width)
         .attr('height', ENTITY_BOX.height)
         .attr('class', 'text-container');

      const nodeText = textContainer
         .append('foreignObject')
         .attr('x', 20)
         .attr('y', 42)
         .attr('height', 38)
         .attr('width', 110)
         .append('xhtml:p')
         .attr('class', 'entity-name');

      if (this.data.nodeTemplate) {
         nodeText.append('span')
            .attr('class', 'template-label')
            .text('T');
      }

      nodeText.append('span').text(this.data.name);

      textContainer.append('text')
         .attr('x', 20)
         .attr('y', 35)
         .attr('class', 'entity-icon')
         .style('font-size', '22')
         .attr('fill', this.createdNew ? '#999' : '#0f1b27')
         .text((d) => icons[this.data.classPrettyName]);


      this._errorIconElement = textContainer.append('g')
         .style('font-size', '14');
      this._getIcon(this._errorIconElement);

   }

   generateEntries() {
      const container = this._containerElement.append('g');
      const entriesCoors = ['64,-6', '134,34', '64 ,74', '-6,34'];
      switch (this.data.stepType) {
         case StepType.Input:
            this._generateInput(this._generateConnection(container)).attr('transform', 'translate(' + entriesCoors[1] + ')');
            break;
         case StepType.Output:
            this._generateOutput(this._generateConnection(container)).attr('transform', 'translate(' + entriesCoors[3] + ')');
            break;
         case StepType.Transformation:
            entriesCoors.map((coors: string) => {
               this._generateInput(this._generateOutput(this._generateConnection(container))).attr('transform', 'translate(' + coors + ')');
            });
            break;
         default:
            break;
      }
   }

   private _generateInput(connection: any) {
      const that = this;
      connection.on('mousedown', function () {
         if (that.drawingConnectionStatus.status) {
            return;
         }
         d3Select(this).classed('over2', true);
         that._ngZone.run(() => {
            that.onDrawConnector.emit({
               event: d3Event,
               name: that.data.name
            });
         });
         d3Event.stopPropagation();
      }).classed('origin', true);
      return connection;
   }

   private _generateOutput(connection: any) {
      connection.on('mouseover', function () {
         d3Select(this).classed('over', true);
      }).on('mouseout', function () {
         d3Select(this).classed('over', false);
      }).classed('destination', true);
      return connection;
   }


   private _generateConnection(container: any) {
      const output = container.append('g').attr('class', 'relation-container');
      output.append('rect')
         .attr('width', 12)
         .attr('height', 12)
         .attr('class', 'relation');
      return output;
   }

   private _getIcon(iconElement: any) {
      let icons = 0;
      iconElement.html('');
      if ((this.data.hasErrors || this._serverStepValidation) && !this.createdNew) {
         iconElement.append('text')
            .attr('class', 'error-icon')
            .attr('x', 116)
            .attr('y', 22)
            .text('\uE613');
         icons++;
      }
      if (this.debugResult) {
         if (this.debugResult.error) {
            iconElement.append('text')
               .attr('class', 'error-icon')
               .attr('x', icons > 0 ? 96 : 116)
               .attr('y', 23)
               .style('font-size', '16')
               .text('\ue049');
            icons++;

         } else if (this.debugResult.result) {
            iconElement.append('text')
               .attr('class', 'success-icon')
               .attr('x', icons > 0 ? 96 : 116)
               .attr('y', 22)
               .text('\ue048');
            icons++;
         }
      }
   }
}
