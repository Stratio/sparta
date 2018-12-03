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
import { Store } from '@ngrx/store';
import { StModalButton, StModalResponse, StModalService } from '@stratio/egeo';

import { Observable, Subject } from 'rxjs';
import { cloneDeep as _cloneDeep } from 'lodash';

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
import { KEYS } from '@app/wizard/wizard.constants';
import { ZoomTransform } from '@app/wizard/models/drag';
import { WorkflowData } from '@app/wizard/wizard.models';
import { WizardEditorComponent } from './wizard-editor/wizard-editor.component';
import { InitializeStepService } from '@app/wizard/services/initialize-step.service';

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

  public editorRef: any;

  public showForbiddenError$: Observable<any>;
  private _componentDestroyed = new Subject();

  @ViewChild('editorArea') editorArea: ElementRef;
  @HostListener('document:keydown', ['$event'])
  onKeydownHandler(event: KeyboardEvent) {
    if (this.hiddenContent) {
      return;
    }
    if (event.keyCode === KEYS.ESC_KEYCODE) {
      if (this.selectedNodeNames.length) {
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
      if (this.selectedNodeNames) {
        this._store.dispatch(new wizardActions.UnselectEntityAction());
      }

      this.isPipelinesNodeSelected = false;
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
    this.editorRef = this.editor.getEditorRef();
    this.creationMode$ = this._store.select(fromWizard.isCreationMode);
    this._store
      .select(fromWizard.getSelectedEntityData)
      .takeUntil(this._componentDestroyed)
      .subscribe(nodeModel => {
        this.selectedNodeModel = nodeModel;
        this._cd.markForCheck();
      });
    this.isShowedEntityDetails$ = this._store.select(
      fromWizard.isShowedEntityDetails
    );
    this._store
      .select(fromWizard.getSelectedEntities)
      .takeUntil(this._componentDestroyed)
      .subscribe(names => {
        this.selectedNodeNames = names;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getConsoleDebugEntityData)
      .takeUntil(this._componentDestroyed)
      .subscribe(debugData => {
        this.consoleDebugData = debugData;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getWorkflowPosition)
      .takeUntil(this._componentDestroyed)
      .subscribe(position => {
        this.editorPosition = position;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getWorkflowNodes)
      .takeUntil(this._componentDestroyed)
      .subscribe((data: Array<any>) => {
        this.workflowNodes = data;
        this._store.dispatch(new wizardActions.ValidateWorkflowAction());
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getWorkflowEdges)
      .takeUntil(this._componentDestroyed)
      .subscribe((data: Array<WizardEdgeNodes>) => {
        this.workflowEdges = data;
        this._cd.markForCheck();
        this._store.dispatch(new wizardActions.ValidateWorkflowAction());
      });
    this._store
      .select(fromWizard.getSelectedRelation)
      .takeUntil(this._componentDestroyed)
      .subscribe((edge: WizardEdge) => {
        this.selectedEdge = edge;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getServerStepValidation)
      .takeUntil(this._componentDestroyed)
      .subscribe((serverStepValidations: any) => {
        this.serverStepValidations = serverStepValidations;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.showDebugConsole)
      .takeUntil(this._componentDestroyed)
      .subscribe((showDebugConsole: any) => {
        this.showDebugConsole = showDebugConsole;
        this._cd.markForCheck();
      });

    this._store
      .select(fromWizard.getDebugResult)
      .takeUntil(this._componentDestroyed)
      .subscribe(debugResult => {
        this.genericError =
          debugResult && debugResult.genericError
            ? debugResult.genericError
            : null;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getEdgeOptions)
      .takeUntil(this._componentDestroyed)
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
      .takeUntil(this._componentDestroyed)
      .subscribe(isDebugging => {
        this.isWorkflowDebugging = isDebugging;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getMultiselectionMode)
      .takeUntil(this._componentDestroyed)
      .subscribe(mode => {
        this.multiselectionMode = mode;
        this._cd.markForCheck();
      });
    this._store
      .select(fromWizard.getDebugResult)
      .takeUntil(this._componentDestroyed)
      .subscribe(debugResult => {
        this.debugResult =
          debugResult && debugResult.steps ? debugResult.steps : {};
        this._cd.markForCheck();
      });
    this.showForbiddenError$ = this.store.select(fromRoot.showPersistentError);
  }

  deleteSelection() {
    if (this.selectedNodeNames && this.selectedNodeNames.length) {
      this.deleteConfirmModal(
        'Delete node',
        'This node and its relations will be deleted.',
        () => {
          this._store.dispatch(new wizardActions.DeleteEntityAction());
          this.isPipelinesNodeSelected = false;
        }
      );
    }
    if (this.selectedEdge) {
      this._store.dispatch(
        new wizardActions.DeleteNodeRelationAction(this.selectedEdge)
      );
    }
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

    const isNodeSelected =
      this.selectedNodeModel && Object.keys(this.selectedNodeModel).length;
    this.isPipelinesNodeSelected =
      isNodeSelected && this.selectedNodeModel.classPrettyName === 'MlPipeline';
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
      .take(1)
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

  deselectEntityCreation() {
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

  duplicateNode(): void {
    if (this.selectedNodeNames.length) {
      const data = _cloneDeep(
        this.workflowNodes.find(
          (node: any) => node.name === this.selectedNodeNames[this.selectedNodeNames.length - 1]
        )
      );
      data.name = this._initializeStepService.getNewEntityName(
        data.name,
        this.workflowNodes
      );
      const newEntity: any = {
        type: 'copy',
        data: data
      };
      this._store.dispatch(new wizardActions.DuplicateNodeAction(newEntity));
    }
  }

  ngOnDestroy(): void {
    this.store.dispatch(new errorsActions.ChangeRouteAction());
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
  }
}
