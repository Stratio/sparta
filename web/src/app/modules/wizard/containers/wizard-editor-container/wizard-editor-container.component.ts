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
   HostListener,
   Input,
   OnDestroy,
   OnInit,
   ViewChild
} from '@angular/core';
import { Store } from '@ngrx/store';
import { StModalButton, StModalResponse, StModalService } from '@stratio/egeo';

import 'rxjs/add/operator/takeUntil';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { cloneDeep as _cloneDeep } from 'lodash';

import * as fromWizard from './../../reducers';
import * as wizardActions from './../../actions/wizard';
import { isMobile } from 'constants/global';
import { WizardNode, WizardEdge, WizardEdgeNodes } from '@app/wizard/models/node';
import { KEYS } from '@app/wizard/wizard.constants';
import { ZoomTransform } from '@app/wizard/models/drag';
import { WizardEditorService, WizardEditorComponent } from '@app/wizard';

@Component({
   selector: 'wizard-editor-container',
   styleUrls: ['wizard-editor-container.component.scss'],
   templateUrl: 'wizard-editor-container.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardEditorContainer implements OnInit, OnDestroy {

   @Input() workflowType = '';
   @ViewChild(WizardEditorComponent) editor: WizardEditorComponent;
   public selectedNodeName = '';
   public selectedNodeModel: WizardNode;
   public selectedEdge: WizardEdge;
   public isMobile = false;
   public workflowName = '';
   public workflowVersion = '';
   public workflowEdges: Array<WizardEdgeNodes>;
   public workflowNodes: Array<WizardNode>;
   public svgPosition: ZoomTransform;
   public creationMode$: Observable<any>;
   public isShowedEntityDetails$: Observable<any>;

   private componentDestroyed = new Subject();

   @ViewChild('editorArea') editorArea: ElementRef;
   @HostListener('document:keydown', ['$event']) onKeydownHandler(event: KeyboardEvent) {
      if (event.keyCode === KEYS.ESC_KEYCODE) {
         if (this.selectedNodeName.length) {
            this.store.dispatch(new wizardActions.UnselectEntityAction());
         }
         this.store.dispatch(new wizardActions.DeselectedCreationEntityAction());

      } else if (event.keyCode === KEYS.SUPR_KEYCODE) {
         this.deleteSelection();
      }
   }

   @HostListener('click', ['$event'])
   clickout(event: any) {
      if (this.editorArea.nativeElement.contains(event.target)) {
         if (this.selectedEdge) {
            this.store.dispatch(new wizardActions.UnselectSegmentAction());
         }
         if (this.selectedNodeName) {
            this.store.dispatch(new wizardActions.UnselectEntityAction());
         }
      }
   }

   constructor(private _modalService: StModalService,
      private editorService: WizardEditorService,
      private _cd: ChangeDetectorRef,
      private store: Store<fromWizard.State>) {
      this.isMobile = isMobile;
   }

   ngOnInit(): void {
      this.initData();
   }

   initData() {
      this.store.select(fromWizard.getWorkflowHeaderData)
         .takeUntil(this.componentDestroyed)
         .subscribe((data: any) => {
            this.workflowName = data.name;
            this.workflowVersion = data.version;
            this._cd.markForCheck();
         });
      this.creationMode$ = this.store.select(fromWizard.isCreationMode);
      this.store.select(fromWizard.getSelectedEntityData)
         .takeUntil(this.componentDestroyed)
         .subscribe(nodeModel => {
            this.selectedNodeModel = nodeModel;
            this._cd.markForCheck();
         });
      this.isShowedEntityDetails$ = this.store.select(fromWizard.isShowedEntityDetails);
      this.store.select(fromWizard.getSelectedEntities)
         .takeUntil(this.componentDestroyed)
         .subscribe(name => {
            this.selectedNodeName = name;
            this._cd.markForCheck();
         });
      this.store.select(fromWizard.getWorkflowPosition)
         .takeUntil(this.componentDestroyed)
         .subscribe(position => {
            this.svgPosition = position;
            this._cd.markForCheck();
         });
      this.store.select(fromWizard.getWorkflowNodes)
         .takeUntil(this.componentDestroyed)
         .subscribe((data: Array<any>) => {
            this.workflowNodes = data;
            this.store.dispatch(new wizardActions.ValidateWorkflowAction());
            this._cd.markForCheck();
         });
      this.store.select(fromWizard.getWorkflowEdges)
         .takeUntil(this.componentDestroyed)
         .subscribe((data: Array<WizardEdgeNodes>) => {
            this.workflowEdges = data;
            this._cd.markForCheck();
            this.store.dispatch(new wizardActions.ValidateWorkflowAction());
         });
      this.store.select(fromWizard.getSelectedRelation)
         .takeUntil(this.componentDestroyed)
         .subscribe((edge: WizardEdge) => {
            this.selectedEdge = edge;
            this._cd.markForCheck();
         });
   }

   deleteSelection() {
      if (this.selectedNodeName && this.selectedNodeName.length) {
         this.deleteConfirmModal('Delete node', 'This node and its relations will be deleted.', () => {
            this.store.dispatch(new wizardActions.DeleteEntityAction());
         });
      }
      if (this.selectedEdge) {
         this.store.dispatch(new wizardActions.DeleteNodeRelationAction(this.selectedEdge));
      }
   }

   selectNode(entity: any) {
      if (this.selectedEdge) {
         this.store.dispatch(new wizardActions.UnselectSegmentAction());
      }
      if (this.selectedNodeName !== entity.name) {
         this.store.dispatch(new wizardActions.SelectEntityAction(entity.name));
      }
   }

   public deleteConfirmModal(modalTitle: string, modalMessage: string, handler: any): void {
      const buttons: StModalButton[] = [
         { label: 'Cancel', responseValue: StModalResponse.NO, closeOnClick: true, classes: 'button-secondary-gray' },
         { label: 'Delete', responseValue: StModalResponse.YES, classes: 'button-critical', closeOnClick: true }
      ];
      this._modalService.show({
         modalTitle: modalTitle,
         buttons: buttons,
         maxWidth: 500,
         messageTitle: 'Are you sure?',
         message: modalMessage,
      }).subscribe((response: any) => {
         if (response === 1) {
            this._modalService.close();
         } else if (response === 0) {
            handler();
         }
      });
   }

   saveWorkflow(closeOnSave: boolean): void {
      this.store.dispatch(new wizardActions.SaveWorkflowPositionsAction(this.workflowNodes));
      this.store.dispatch(new wizardActions.SaveEditorPosition(this.editor._svgPosition));
      this.store.dispatch(new wizardActions.SaveWorkflowAction(closeOnSave));
   }

   createNode(event: any) {
      this.store.dispatch(new wizardActions.CreateEntityAction(event));
   }

   createEdge(event: any) {
      this.store.dispatch(new wizardActions.CreateNodeRelationAction(event));
   }

   closeSideBar() {
      this.store.dispatch(new wizardActions.ToggleDetailSidebarAction());
   }

   editButtonEntity() {
      this.editEntity(this.selectedNodeModel);
   }

   editEntity(entity: any) {
      this.store.dispatch(new wizardActions.SaveWorkflowPositionsAction(this.workflowNodes));
      this.store.dispatch(new wizardActions.ShowEditorConfigAction({
         stepType: entity.stepType,
         data: entity
      }));
   }

   duplicateNode(): void {
      if (this.selectedNodeName) {
         const data = _cloneDeep(this.workflowNodes.find((node: any) => node.name === this.selectedNodeName));
         data.name = this.editorService.getNewEntityName(data.name, this.workflowNodes);
         const newEntity: any = {
            type: 'copy',
            data: data
         };
         this.store.dispatch(new wizardActions.DuplicateNodeAction(newEntity));
      }
   }

   ngOnDestroy(): void {
      this.componentDestroyed.next();
      this.componentDestroyed.unsubscribe();
   }
}
