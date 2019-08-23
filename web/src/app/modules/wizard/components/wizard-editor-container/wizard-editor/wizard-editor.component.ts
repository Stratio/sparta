/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  EventEmitter,
  Output,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { WizardNode, WizardEdge } from '@app/wizard/models/node';
import { ZoomTransform, DrawingConnectorStatus } from '@app/wizard/models/drag';
import { WizardEdgeModel } from '@app/wizard/components/wizard-edge/wizard-edge.model';
import { GraphEditorComponent } from '@app/shared/components/graph-editor/graph-editor.component';
import { WizardAnnotation, annotationColors } from '@app/shared/wizard/components/wizard-annotation/wizard-annotation.model';
import { WizardAnnotationService } from '@app/shared/wizard/components/wizard-annotation/wizard-annotation.service';

import { ENTITY_BOX } from './../../../wizard.constants';
import { DraggableElementPosition } from '@app/shared';

@Component({
  selector: 'wizard-editor',
  styleUrls: ['wizard-editor.component.scss'],
  templateUrl: 'wizard-editor.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardEditorComponent {

  @Input() workflowNodes: Array<WizardNode> = [];
  @Input() workflowEdges: Array<WizardEdgeModel> = [];
  @Input() editorPosition: ZoomTransform;
  @Input() serverStepValidations: any = {};
  @Input() selectedNodeNames = [];
  @Input() multiselectionMode: boolean;
  @Input() draggableMode: boolean;
  @Input() creationMode: any;
  @Input() selectedEdge: any;
  @Input() debugResult: any;
  @Input() disableEvents: boolean;
  @Input() multiDrag: boolean;
  @Input() isCreatingNoteActive: boolean;
  @Input() nodeAnnotationsMap: { [nodeName: string]: WizardAnnotation };
  @Input() edgeAnnotationsMap: { [key: string]: WizardAnnotation };
  @Input() draggableAnnotations: WizardAnnotation;

  @Output() setEditorDirty = new EventEmitter();
  @Output() disableSelection = new EventEmitter();
  @Output() createNode = new EventEmitter<any>();
  @Output() onCreateEdge = new EventEmitter<WizardEdge>();
  @Output() selectNode = new EventEmitter<WizardNode>();
  @Output() editEntity = new EventEmitter<WizardNode>();
  @Output() showEdgeOptions = new EventEmitter<any>();
  @Output() selectEdge = new EventEmitter<any>();
  @Output() editorPositionChange = new EventEmitter<ZoomTransform>();
  @Output() selectNodes = new EventEmitter<Array<string>>();
  @Output() nodePositionChange = new EventEmitter<{ position: DraggableElementPosition, nodeName: string }>();
  @Output() tipPositionChange = new EventEmitter<{ position: DraggableElementPosition, number: number }>();
  @Output() showAnnotation = new EventEmitter<WizardAnnotation>();
  @Output() createAnnotation = new EventEmitter<WizardAnnotation>();

  @ViewChild(GraphEditorComponent) editor: GraphEditorComponent;

  public drawingConnectionStatus: DrawingConnectorStatus = {
    status: false,
    name: ''
  };

  public annotationPosition = { x: 0, y: 0 };
  public connectorOrigin = '';
  public connectorPosition: ZoomTransform = null;
  public initialSelectionCoors: any;

  constructor(
    private _cd: ChangeDetectorRef,
    private _el: ElementRef,
    private _wizardAnnotationService: WizardAnnotationService
  ) { }

  createEdge(edgeEvent) {
    this.connectorOrigin = edgeEvent.name;
    this.drawingConnectionStatus = {
      status: true,
      name: edgeEvent.name,
      initPosition: {
        x: edgeEvent.event.clientX,
        y: edgeEvent.event.clientY
      }
    };
  }

  removeConnector() {
    this.connectorOrigin = '';
    this.drawingConnectionStatus = {
      status: false,
      name: ''
    };
  }

  finishConnector(destinationEntity: any) {
    if (this.connectorOrigin.length) {
      this.onCreateEdge.emit({
        origin: this.connectorOrigin,
        destination: destinationEntity.name
      });
    }
  }

  getEditorRef() {
    return this.editor;
  }

  trackBySegmentFn(index: number, item: any) {
    return item.origin.name + '////' + item.destination.name;
  }

  onFinishSelection(event) {
    this.initialSelectionCoors = null;
    this._cd.markForCheck();
    const selectedNodes = [];
    const nodes = this._el.nativeElement.querySelectorAll('g[wizard-node]');
    [].forEach.call(nodes, (wNode) => {
      const position = wNode.getBoundingClientRect();
      if ((position.left + ENTITY_BOX.width * this.editorPosition.k) >= event.left && event.right > position.left && (position.top + ENTITY_BOX.height * this.editorPosition.k) >= event.top && event.bottom > position.top) {
        selectedNodes.push(wNode.getAttribute('node-name'));
      }
    });
    this.selectNodes.emit(selectedNodes);
  }

  public onClickEditor(event) {
    if (this.isCreatingNoteActive) {
      this.createDraggableNote(event);
    } else {
      this.disableSelection.emit();
    }
  }
  public createDraggableNote(event) {
    this.createAnnotation.emit(this._wizardAnnotationService.createDraggableNote(event));
  }

   public createEdgeNote(event) {
    this.createAnnotation.emit(this._wizardAnnotationService.createEdgeNote(event));
  }

   public clickNode(nodeData: WizardNode) {
    if (this.isCreatingNoteActive) {
      this.createAnnotation.emit(this._wizardAnnotationService.createStepNote(nodeData.name));
    } else {
      this.selectNode.emit(nodeData);
    }
  }

   public onShowAnnotation(position: { x: number; y: number; }, annotation: WizardAnnotation) {
    this.showAnnotation.emit({
      ...annotation,
      tipPosition: position
    });
  }

   public showEditorAnnotation(event: any, annotation: WizardAnnotation) {
    const target = event.currentTarget.getBoundingClientRect();
    this.onShowAnnotation({
      x: target.x,
      y: target.y
    }, annotation);
  }

   public changeTipPosition(event, number: number) {
    this.tipPositionChange.emit({
      position: event,
      number
    });
  }

   public onEdgeClick(event) {
    if (this.isCreatingNoteActive) {
      this.createEdgeNote(event);
    } else {
      this.selectEdge.emit(event);
    }
  }

   public trackByNoteFn(index: number, annotation: WizardAnnotation) {
    return annotation.number;
  }

}
