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
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Rx';
import * as d3 from 'd3';
import { ZoomBehavior, DragBehavior } from 'd3';
import { cloneDeep as _cloneDeep } from 'lodash';

import * as fromWizard from './../../reducers';
import * as wizardActions from './../../actions/wizard';
import { WizardEditorService } from './wizard-editor.sevice';
import { InitializeSchemaService } from 'services';
import { isMobile } from 'constants/global';
import { WizardNode, WizardEdge } from '@app/wizard/models/node';
import { ZoomTransform, CreationData, NodeConnector } from '@app/wizard/models/drag';

@Component({
   selector: 'wizard-editor',
   styleUrls: ['wizard-editor.styles.scss'],
   templateUrl: 'wizard-editor.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardEditorComponent implements OnInit {

   @Input() workflowType: string;
   @Input() workflowEdges: Array<WizardEdge> = [];
   @Input()
   get svgPosition(): ZoomTransform {
      return this._svgPosition;
   };
   set svgPosition(value) {
      this._svgPosition = _cloneDeep(value);
      if (this._SVGParent) {
         /** Update editor position */
         this._SVGParent.call(this.zoom.transform, d3.zoomIdentity.translate(this._svgPosition.x, this._svgPosition.y)
         .scale(this._svgPosition.k === 0 ? 1 : this._svgPosition.k));
      }
   }
   @Input() workflowNodes: Array<WizardNode> = [];
   @Input() creationMode: CreationData;
   @Input() selectedNodeName = '';
   @Input() selectedEdge: WizardEdge;
   @Output() editEntity = new EventEmitter<WizardNode>();
   @Output() selectNode = new EventEmitter<WizardNode>();
   @Output() createNode = new EventEmitter<any>();
   @Output() onCreateEdge = new EventEmitter<any>();

   public entitiesData: WizardNode;
   public isMobile = false;
   public showConnector = false;

   /** Selectors */
   private _SVGParent: d3.Selection<d3.BaseType, any, any, any>;
   private _SVGContainer: d3.Selection<d3.BaseType, any, any, any>;
   private _documentRef: d3.Selection<d3.BaseType, any, any, any>;
   private _connectorElement: d3.Selection<d3.BaseType, any, any, any>;

   public drawingConnectionStatus: any = {
      status: false,
      name: ''
   };
   public isShowedEntityDetails$: Observable<boolean>;
   public isPristine = true;
   public _svgPosition: ZoomTransform;

   private newOrigin = '';
   private zoom: ZoomBehavior<any, any>;
   private drag: DragBehavior<any, any, any>;

   constructor(private _elementRef: ElementRef,
      private _editorService: WizardEditorService,
      private _store: Store<fromWizard.State>,
      private _cd: ChangeDetectorRef,
      private initializeSchemaService: InitializeSchemaService,
      private _ngZone: NgZone) {
      this.isMobile = isMobile;
   }

   ngOnInit(): void {
      this._initSelectors();
      function deltaFn() {
         return -d3.event.deltaY * (d3.event.deltaMode ? 0.0387 : 0.002258);
      }
      this.zoom = d3.zoom()
         .scaleExtent([1 / 8, 3])
         .wheelDelta(deltaFn);
      this.drag = d3.drag();
      this._ngZone.runOutsideAngular(() => {
         this.setDraggableEditor();
      });
      // Set initial position
      this._SVGParent.call(this.zoom.transform, d3.zoomIdentity.translate(this._svgPosition.x, this._svgPosition.y)
         .scale(this._svgPosition.k === 0 ? 1 : this._svgPosition.k));
   }

   private _initSelectors() {
      this._documentRef = d3.select(document);
      const element: d3.Selection<Element, any, any, any> = d3.select(this._elementRef.nativeElement);
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
      this._store.dispatch(new wizardActions.DeselectedCreationEntityAction());
   }

   createEdge(event: any) {
      if (isMobile) {
         this.newOrigin = event.name;
         this.drawingConnectionStatus = {
            status: true,
            name: event.name
         };
         const w = this._documentRef
            .on('click', () => {
               w.on('click', null).on('click', null);
               this.newOrigin = '';
               event.event.target.classList.remove('over-output2');
               this.drawingConnectionStatus = {
                  status: false
               };
            });
      } else {
         this.drawConnector(event);
      }
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
         event.event.target.classList.remove('over-output2');
         this.showConnector = false;
         this.newOrigin = '';
         this.drawingConnectionStatus = {
            status: false
         };
         w.on('mousemove', null).on('mouseup', null);
         this._cd.markForCheck();
      }

      function drawConnector() {
         connector.x2 = d3.event.clientX - connector.x1;
         connector.y2 = d3.event.clientY - connector.y1 - 135;
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
      const translateX = ((svgWidth - containerWidth) / 2 - container.left) / this._svgPosition.k;
      const translateY = ((svgHeight - containerHeight) / 2 - container.top) / this._svgPosition.k;
      this._SVGParent.call(this.zoom.translateBy, translateX, translateY + 135 / this._svgPosition.k);
   }

   trackBySegmentFn(index: number, item: any) {
      return index; // or item.id
   }

   setDraggableEditor() {
      this.setDragStart();
      let pristine = true;
      let repaints = 0;
      const _SVGContainer = this._SVGContainer;
      const repaint = function () {
         _SVGContainer.attr('transform', this.toString());
      };
      let lastUpdateCall: number;
      this._SVGParent.call(this.zoom.on('zoom', (el: SVGSVGElement) => {
         const e: any = d3.event;
         if (pristine) {
            if (repaints < 2) {
               repaints++;
            } else {
               if (this.workflowNodes.length) {
                  pristine = false;
                  this._store.dispatch(new wizardActions.SetWizardStateDirtyAction());
               }
            }
         }
         if (lastUpdateCall) {
            cancelAnimationFrame(lastUpdateCall);
         }
         this._svgPosition = e.transform;
         lastUpdateCall = requestAnimationFrame(repaint.bind(e.transform));
      })).on('dblclick.zoom', null);
   }

   setDragStart() {
      this._SVGParent.call(this.drag
         .on('start', () => {
            const event = d3.event;
            const position = {
               offsetX: event.x,
               offsetY: event.y
            };
            this.clickDetected.call(this, position);
         }));
   }
}
