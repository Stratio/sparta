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
import { WizardNode, WizardEdge, WizardEdgeNodes, EdgeOption } from '@app/wizard/models/node';
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
   @Input() hiddenContent: boolean;
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
   public edgeOptions: EdgeOption;
   public isWorkflowDebugging: boolean;
   public debugResult: any;
   public serverStepValidations: any;
   public showDebugConsole: boolean;
   public genericError: any;
   private _componentDestroyed = new Subject();

   @ViewChild('editorArea') editorArea: ElementRef;
   @HostListener('document:keydown', ['$event']) onKeydownHandler(event: KeyboardEvent) {
      if (this.hiddenContent) {
        return;
      }
      if (event.keyCode === KEYS.ESC_KEYCODE) {
         if (this.selectedNodeName.length) {
            this._store.dispatch(new wizardActions.UnselectEntityAction());
         }
         this._store.dispatch(new wizardActions.DeselectedCreationEntityAction());

      } else if (event.keyCode === KEYS.SUPR_KEYCODE) {
         this.deleteSelection();
      }
   }

   @HostListener('click', ['$event'])
   clickout(event: any) {
      if (this.editorArea.nativeElement.contains(event.target)) {
         if (this.selectedEdge) {
            this._store.dispatch(new wizardActions.UnselectSegmentAction());
         }
         if (this.selectedNodeName) {
            this._store.dispatch(new wizardActions.UnselectEntityAction());
         }
      }
   }

   constructor(private _modalService: StModalService,
      private _editorService: WizardEditorService,
      private _cd: ChangeDetectorRef,
      private _store: Store<fromWizard.State>,
      private _el: ElementRef) {
      this.isMobile = isMobile;
   }

   ngOnInit(): void {
      this.initData();
   }

   initData() {
      this._store.select(fromWizard.getWorkflowHeaderData)
         .takeUntil(this._componentDestroyed)
         .subscribe((data: any) => {
            this.workflowName = data.name;
            this.workflowVersion = data.version;
            this._cd.markForCheck();
         });
      this.creationMode$ = this._store.select(fromWizard.isCreationMode);
      this._store.select(fromWizard.getSelectedEntityData)
         .takeUntil(this._componentDestroyed)
         .subscribe(nodeModel => {
            this.selectedNodeModel = nodeModel;
            this._cd.markForCheck();
         });
      this.isShowedEntityDetails$ = this._store.select(fromWizard.isShowedEntityDetails);
      this._store.select(fromWizard.getSelectedEntities)
         .takeUntil(this._componentDestroyed)
         .subscribe(name => {
            this.selectedNodeName = name;
            this._cd.markForCheck();
         });
      this._store.select(fromWizard.getWorkflowPosition)
         .takeUntil(this._componentDestroyed)
         .subscribe(position => {
            this.svgPosition = position;
            this._cd.markForCheck();
         });
      this._store.select(fromWizard.getWorkflowNodes)
         .takeUntil(this._componentDestroyed)
         .subscribe((data: Array<any>) => {
            this.workflowNodes = data;
            this._store.dispatch(new wizardActions.ValidateWorkflowAction());
            this._cd.markForCheck();
         });
      this._store.select(fromWizard.getWorkflowEdges)
         .takeUntil(this._componentDestroyed)
         .subscribe((data: Array<WizardEdgeNodes>) => {
            this.workflowEdges = data;
            this._cd.markForCheck();
            this._store.dispatch(new wizardActions.ValidateWorkflowAction());
         });
      this._store.select(fromWizard.getSelectedRelation)
         .takeUntil(this._componentDestroyed)
         .subscribe((edge: WizardEdge) => {
            this.selectedEdge = edge;
            this._cd.markForCheck();
         });
      this._store.select(fromWizard.getServerStepValidation)
         .takeUntil(this._componentDestroyed)
         .subscribe((serverStepValidations: any) => {
            this.serverStepValidations = serverStepValidations;
            this._cd.markForCheck();
         });
      this._store.select(fromWizard.showDebugConsole)
         .takeUntil(this._componentDestroyed)
         .subscribe((showDebugConsole: any) => {
            this.showDebugConsole = showDebugConsole;
            this._cd.markForCheck();
         });

      this._store.select(fromWizard.getDebugResult)
         .takeUntil(this._componentDestroyed)
         .subscribe(debugResult => {
            this.genericError = debugResult && debugResult.genericError ? debugResult.genericError : null;
            this._cd.markForCheck();
         });
      this._store.select(fromWizard.getEdgeOptions)
         .takeUntil(this._componentDestroyed)
         .subscribe((edgeOptions: any) => {
            if (this.edgeOptions && !edgeOptions.active && !this.edgeOptions.active) {
               return;
            }
            this.edgeOptions = edgeOptions;
            /** sync hide element, sometimes current event cascade is ocuppied
             * by the drag events and the if operator can't be executed until the dragend*/

            const dropdownElement = this._el.nativeElement.querySelector('#edge-dropdown');
            if (dropdownElement) {
               dropdownElement.hidden = !this.edgeOptions.active;
            }

            this._cd.markForCheck();
         });
      this._store.select(fromWizard.isWorkflowDebugging)
         .takeUntil(this._componentDestroyed)
         .subscribe((isDebugging => {
            this.isWorkflowDebugging = isDebugging;
            this._cd.markForCheck();
         }));

      this._store.select(fromWizard.getDebugResult)
         .takeUntil(this._componentDestroyed)
         .subscribe((debugResult => {
            this.debugResult = debugResult && debugResult.steps ? debugResult.steps : {};
            this._cd.markForCheck();
         }));
   }

   deleteSelection() {
      if (this.selectedNodeName && this.selectedNodeName.length) {
         this.deleteConfirmModal('Delete node', 'This node and its relations will be deleted.', () => {
            this._store.dispatch(new wizardActions.DeleteEntityAction());
         });
      }
      if (this.selectedEdge) {
         this._store.dispatch(new wizardActions.DeleteNodeRelationAction(this.selectedEdge));
      }
   }

   selectNode(entity: any) {
      if (this.selectedEdge) {
         this._store.dispatch(new wizardActions.UnselectSegmentAction());
      }
      if (this.selectedNodeName !== entity.name) {
         this._store.dispatch(new wizardActions.SelectEntityAction(entity.name));
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
      this._store.dispatch(new wizardActions.SaveWorkflowPositionsAction(this.workflowNodes));
      this._store.dispatch(new wizardActions.SaveEditorPosition(this.editor._svgPosition));
      this._store.dispatch(new wizardActions.SaveWorkflowAction(closeOnSave));
   }

   createNode(event: any) {
      this._store.dispatch(new wizardActions.CreateEntityAction(event));
   }

   createEdge(event: any) {
      this._store.dispatch(new wizardActions.CreateNodeRelationAction(event));
   }

   closeSideBar() {
      this._store.dispatch(new wizardActions.ToggleDetailSidebarAction());
   }

   editButtonEntity() {
      this.editEntity(this.selectedNodeModel);
   }

   editEntity(entity: any) {
      this._store.dispatch(new wizardActions.SaveWorkflowPositionsAction(this.workflowNodes));
      this._store.dispatch(new wizardActions.ShowEditorConfigAction({
         stepType: entity.stepType,
         data: entity
      }));
   }

   duplicateNode(): void {
      if (this.selectedNodeName) {
         const data = _cloneDeep(this.workflowNodes.find((node: any) => node.name === this.selectedNodeName));
         data.name = this._editorService.getNewEntityName(data.name, this.workflowNodes);
         const newEntity: any = {
            type: 'copy',
            data: data
         };
         this._store.dispatch(new wizardActions.DuplicateNodeAction(newEntity));
      }
   }

   ngOnDestroy(): void {
      this._componentDestroyed.next();
      this._componentDestroyed.unsubscribe();
   }
}
