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
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import { Store, select } from '@ngrx/store';
import { StModalButton, StModalResponse, StModalService } from '@stratio/egeo';

import { Observable, Subject } from 'rxjs';

import * as fromWizard from './../../reducers';
import * as fromRoot from 'reducers';
import * as errorsActions from 'actions/errors';
import * as wizardActions from './../../actions/wizard';

import { isMobile } from 'constants/global';
import {
  WizardNode,
  WizardEdge,
  WizardEdgeNodes,
  EdgeOption
} from '@app/wizard/models/node';
import { ZoomTransform } from '@app/wizard/models/drag';
import { WorkflowData } from '@app/wizard/models/data';
import { WizardEditorComponent } from './wizard-editor/wizard-editor.component';
import { InitializeStepService } from '@app/wizard/services/initialize-step.service';

import { takeUntil, take } from 'rxjs/operators';

@Component({
  selector: 'wizard-editor-container',
  styleUrls: ['wizard-editor-container.component.scss'],
  templateUrl: 'wizard-editor-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WizardEditorContainer implements OnInit, OnDestroy {
  @Input() workflowData: WorkflowData;
  @Input() hiddenContent: boolean;

  @ViewChild(WizardEditorComponent) editor: WizardEditorComponent;
  @ViewChild('wizardModal', { read: ViewContainerRef }) target: any;

  public selectedNodeNames = [];
  public selectedNodeModel: WizardNode | any;
  public selectedEdge: WizardEdge;
  public isMobile = false;
  public workflowEdges: Array<WizardEdgeNodes>;
  public workflowNodes: Array<WizardNode>;

  public editorPosition: ZoomTransform;
  public creationMode$: Observable<any>;
  public isShowedEntityDetails$: Observable<any>;
  public edgeOptions: EdgeOption;
  public isWorkflowDebugging: boolean;
  public debugResult: any;
  public serverStepValidations: any;
  public showDebugConsole: boolean;
  public genericError: any;
  public consoleDebugData: any;
  public isPipelinesNodeSelected: boolean;
  public multiselectionMode: boolean;
  public draggableMode: boolean;
  public multiDrag: boolean;

  public editorRef: any;

  public showForbiddenError$: Observable<any>;
  private _componentDestroyed = new Subject();
  private _ctrlDown = false;
  @ViewChild('editorArea') editorArea: ElementRef;
  @HostListener('document:keydown', ['$event'])
  onKeydownHandler(event: KeyboardEvent) {
    if (this.hiddenContent) {
      return;
    }
    switch (event.keyCode) {
      /** CTRL */
      case 17: {
        this._ctrlDown = true;
        this._store.dispatch(new wizardActions.SetMultiselectionModeAction(true));
        break;
      }
      /** ESC */
      case 27: {
        if (this.selectedNodeNames.length) {
          this._store.dispatch(new wizardActions.UnselectEntityAction());
        }
        this._store.dispatch(new wizardActions.DeselectedCreationEntityAction());
        break;
      }
      /** SUPR */
      case 46: {
        this.deleteSelection();
        break;
      }
      /**  CTRL + C */
      case 67: {
        if (!this._ctrlDown) {
          return;
        }
        // save current workflow positions in the store and dispatch copy action on the next tick
        this._store.dispatch(new wizardActions.SaveWorkflowPositionsAction(this.workflowNodes));
        this._store.dispatch(new wizardActions.SaveEditorPosition(this.editorRef.editorPosition));
        setTimeout(() => this._store.dispatch(new wizardActions.CopyNodesAction()));
        break;
      }
      /** CTRL +  V */
      case 86: {
        if (!this._ctrlDown) {
          return;
        }
        // save current workflow positions in the store and dispatch copy action on the next tick
        this._store.dispatch(new wizardActions.SaveWorkflowPositionsAction(this.workflowNodes));
        this._store.dispatch(new wizardActions.SaveEditorPosition(this.editorRef.editorPosition));
        setTimeout(() => this._store.dispatch(new wizardActions.PasteNodesAction()));
        break;
      }
      /** SPACE and ALT*/
      case 32: case 18: {
        this.multiDrag = true;
        break;
      }
    }
  }

  @HostListener('document:keyup', ['$event'])
  onKeyupHandler(event: KeyboardEvent) {
    switch (event.keyCode) {
      case 17: {
        this._ctrlDown = false;
        this._store.dispatch(new wizardActions.SetMultiselectionModeAction(false));
        break;
      }
      /** SPACE and ALT*/
      case 32: case 18: {
        this.multiDrag = false;
        break;
      }
    }
  }

  constructor(
    private _modalService: StModalService,
    private _initializeStepService: InitializeStepService,
    private _cd: ChangeDetectorRef,
    private _store: Store<fromWizard.State>,
    private _el: ElementRef,
    private store: Store<fromRoot.State>
  ) {
    this.isMobile = isMobile;
  }

  ngOnInit(): void {
    this._onBlur = this._onBlur.bind(this);
    window.addEventListener('blur', this._onBlur);
    this.editorRef = this.editor.getEditorRef();
    this.creationMode$ = this._store.pipe(select(fromWizard.isCreationMode));
    this._store
      .pipe(select(fromWizard.getSelectedEntityData))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(nodeModel => {
        this.selectedNodeModel = nodeModel;
        this._cd.markForCheck();
      });
    this.isShowedEntityDetails$ = this._store.pipe(select(
      fromWizard.isShowedEntityDetails
    ));
    this._store
      .select(fromWizard.getSelectedEntities)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((names: Array<string>) => {
        this.selectedNodeNames = names;
        this._cd.markForCheck();
      });
      this._store
        .select(fromWizard.isPipelinesNodeSelected)
        .pipe(takeUntil(this._componentDestroyed))
        .subscribe((isSelected) => {
          this.isPipelinesNodeSelected = isSelected;
          this._cd.markForCheck();
        });
    this._store
      .select(fromWizard.getWorkflowPosition)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((position: ZoomTransform) => {
        this.editorPosition = position;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getWorkflowNodes)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((data: Array<any>) => {
        this.workflowNodes = data;
        this._store.dispatch(new wizardActions.ValidateWorkflowAction());
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getWorkflowEdges)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((data: Array<WizardEdgeNodes>) => {
        this.workflowEdges = data;
        this._cd.markForCheck();
        this._store.dispatch(new wizardActions.ValidateWorkflowAction());
      });
    this._store
      .select(fromWizard.getSelectedRelation)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((edge: WizardEdge) => {
        this.selectedEdge = edge;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getServerStepValidation)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((serverStepValidations: any) => {
        this.serverStepValidations = serverStepValidations;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getEdgeOptions)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((edgeOptions: any) => {
        if (
          this.edgeOptions &&
          !edgeOptions.active &&
          !this.edgeOptions.active
        ) {
          return;
        }
        this.edgeOptions = edgeOptions;
        /** sync hide element, sometimes current event cascade is ocuppied
         * by the drag events and the if operator can't be executed until the dragend*/
        const dropdownElement = this._el.nativeElement.querySelector(
          '#edge-dropdown'
        );
        if (dropdownElement) {
          dropdownElement.hidden = !this.edgeOptions.active;
        }
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.isWorkflowDebugging)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((isDebugging: boolean) => {
        this.isWorkflowDebugging = isDebugging;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getMultiselectionMode)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((mode: boolean) => {
        this.multiselectionMode = mode;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getDebugResult)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((debugResult: any) => {
        this.debugResult =
          debugResult && debugResult.steps ? debugResult.steps : {};
        this._cd.markForCheck();
      });
    this.showForbiddenError$ = this.store.pipe(select(fromRoot.showPersistentError));
  }

  deleteSelection() {
    if (this.selectedNodeNames && this.selectedNodeNames.length) {
      this.deleteConfirmModal(
        'Delete node',
        'This nodes and its relations will be deleted: ' + this.selectedNodeNames.join(', '),
        () => {
          this._store.dispatch(new wizardActions.DeleteEntityAction());
        }
      );
    }
    if (this.selectedEdge) {
      this._store.dispatch(
        new wizardActions.DeleteNodeRelationAction(this.selectedEdge)
      );
    }
  }

  editorPositionChange(position) {
    this.editorPosition = position;
  }

  selectNode(entity: WizardNode) {
    if (this.selectedEdge) {
      this._store.dispatch(new wizardActions.UnselectSegmentAction());
    }
    const isPipelinesNodeEdition =
      entity.classPrettyName && entity.classPrettyName === 'MlPipeline';
    this._store.dispatch(
      new wizardActions.SelectEntityAction(
        entity.name,
        isPipelinesNodeEdition
      )
    );
  }

  public deleteConfirmModal(
    modalTitle: string,
    modalMessage: string,
    handler: any
  ): void {
    const buttons: StModalButton[] = [
      {
        label: 'Cancel',
        responseValue: StModalResponse.NO,
        closeOnClick: true,
        classes: 'button-secondary-gray'
      },
      {
        label: 'Delete',
        responseValue: StModalResponse.YES,
        classes: 'button-critical',
        closeOnClick: true
      }
    ];
    this._modalService.container = this.target;
    this._modalService
      .show({
        modalTitle: modalTitle,
        buttons: buttons,
        maxWidth: 500,
        messageTitle: 'Are you sure?',
        message: modalMessage
      })
      .pipe(take(1))
      .subscribe((response: any) => {
        if (response === 1) {
          this._modalService.close();
        } else if (response === 0) {
          handler();
        }
      });
  }

  saveWorkflow(closeOnSave: boolean): void {
    this._store.dispatch(
      new wizardActions.SaveWorkflowPositionsAction(this.workflowNodes)
    );
    this._store.dispatch(
      new wizardActions.SaveEditorPosition(this.editorRef.editorPosition)
    );
    this._store.dispatch(new wizardActions.SaveWorkflowAction(closeOnSave));
  }

  createNode(event: any) {
    const creationMode = event.creationMode;
    let entity: any = {};
    const entityData = creationMode.data;
    if (entityData.type === 'copy') {
      // if its a copy, only xsets the position
      entity = entityData.data;
    } else {
      entity = this._initializeStepService.initializeEntity(
        this.workflowData.type,
        entityData,
        this.workflowNodes
      );
    }
    entity.uiConfiguration = {
      position: event.position
    };
    this._store.dispatch(new wizardActions.CreateEntityAction(entity));
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

  editButtonPipelinesEntity() {
    this._store.dispatch(new wizardActions.ModifyIsPipelinesNodeEdition(false));
    this.editEntity(this.selectedNodeModel);
  }

  setEditorDirty() {
    this._store.dispatch(new wizardActions.SetWizardStateDirtyAction());
  }

  selectEdge(event) {
    this._store.dispatch(new wizardActions.SelectSegmentAction(event));
    this.isPipelinesNodeSelected = false;
  }

  disableSelection() {
    this._store.dispatch(new wizardActions.DeselectedCreationEntityAction());
  }

  editEntity(entity: any) {
    this._store.dispatch(
      new wizardActions.SaveWorkflowPositionsAction(this.workflowNodes)
    );
    this._store.dispatch(
      new wizardActions.ShowEditorConfigAction({
        stepType: entity.stepType,
        data: entity
      })
    );
  }

  showEdgeOptions(event) {
    this._store.dispatch(new wizardActions.ShowEdgeOptionsAction(event));
  }

  selectNodes(event: Array<string>) {
    this._store.dispatch(new wizardActions.SelectMultipleStepsAction(event));
  }

  private _onBlur() {
    this._ctrlDown = false;
    this.multiDrag = false;
    this._store.dispatch(new wizardActions.SetMultiselectionModeAction(false));
  }

  ngOnDestroy(): void {
    this.store.dispatch(new errorsActions.ChangeRouteAction());
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
    window.removeEventListener('blur', this._onBlur);
  }
}
