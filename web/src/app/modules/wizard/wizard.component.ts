/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {ChangeDetectorRef, Component, ElementRef, Inject, OnDestroy, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DOCUMENT } from '@angular/common';
import { StModalService } from '@stratio/egeo';

import { Store, select } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';

import * as fromWizard from './reducers';
import * as debugActions from './actions/debug';
import * as wizardActions from './actions/wizard';
import * as externalDataActions from './actions/externalData';

import { WizardService } from './services/wizard.service';
import { Engine } from 'app/models/enums';
import { CreationMode, EditionConfigMode } from './models/node';
import { WorkflowData } from '@app/wizard/models/data';

import { streamingPreprocessingNames, batchPreprocessingNames } from 'data-templates/pipelines/pipelines-preprocessing';
import { streamingAlgorithmNames, batchAlgorithmNames } from 'data-templates/pipelines/pipelines-algorithm';
import { takeUntil } from 'rxjs/operators';
import { WizardHelpCardsComponent } from './components/wizard-help-cards/wizard-help-cards.component';

@Component({
  selector: 'wizard',
  styleUrls: ['wizard.styles.scss'],
  templateUrl: 'wizard.template.html'
})

export class WizardComponent implements OnInit, OnDestroy {
  public workflowType = 'Streaming'; // default value, until the request ends
  public editionConfigMode: EditionConfigMode;
  public showSettings = false;
  public creationMode: CreationMode;
  public isEdit = false;
  public environmentList: Array<any> = [];
  public parameters: any = [];
  public showDebugConfig$: Observable<boolean>;
  public executionContexts$: Observable<any>;
  public mlModelsList: Array<string> = [];

  public workflowData: WorkflowData;
  public pipelinesMenu: any = [
    {
      name: 'Preprocessing',
      icon: 'icon-lego',
      value: 'action',
      subMenus: []
    },
    {
      name: 'Algorithm',
      icon: 'icon-chip',
      value: 'action',
      subMenus: []
    }
  ];

  @ViewChild('editorContainer') _editor: ElementRef;
  @ViewChild('wizardCardsModal', { read: ViewContainerRef }) target: any;

  private _componentDestroyed = new Subject();

  constructor(
    private _store: Store<fromWizard.State>,
    private _route: ActivatedRoute,
    private _cd: ChangeDetectorRef,
    private _stModalService: StModalService,
    @Inject(DOCUMENT) private _document: any,
    private _wizardService: WizardService
  ) {
    this._document.body.classList.add('disable-scroll');
    this.workflowData = {
      name: '',
      type: '',
      version: ''
    };
  }

  ngOnInit(): void {
    const id = this._route.snapshot.params.id;
    this.isEdit = (id && id.length);

    const showHelpCards = localStorage.getItem('sparta-cards-help');
    if (!showHelpCards) {
      this._showCardsModal();
    }

    this._store.dispatch(new externalDataActions.GetParamsListAction());
    this._store.dispatch(new externalDataActions.GetMlModelsListAction());
    this._store.dispatch(new wizardActions.ResetWizardAction(this.isEdit));  // Reset wizard to default settings
    const type = this._route.snapshot.params.type === 'streaming' ? Engine.Streaming : Engine.Batch;
    if (this.isEdit) {
      this._store.dispatch(new wizardActions.ModifyWorkflowAction(id));    // Get workflow data from API and then get the menu templates
      this._store.dispatch(new debugActions.GetDebugResultAction(id));     // Get the last debug result
    } else {
      this._wizardService.workflowType = type;
      this._store.dispatch(new wizardActions.SetWorkflowTypeAction(type)); // Set workflow type from the url param
      this._store.dispatch(new wizardActions.GetMenuTemplatesAction());    // Get menu templates
    }
    this.executionContexts$ = this._store.pipe(select(fromWizard.getExecutionContexts));
    // Retrieves the workflow type from store (in edition mode, is updated after the get workflow data request)
    this._store.pipe(select(fromWizard.getWorkflowType))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((workflowType: string) => {
        this._wizardService.workflowType = workflowType;
        this.workflowType = workflowType;
        this.workflowData.type = workflowType;

        // Construct Menu Categories
        const preprocessingList = (workflowType === Engine.Batch) ? batchPreprocessingNames : streamingPreprocessingNames;
        const algorithmList = (workflowType === Engine.Batch) ? batchAlgorithmNames : streamingAlgorithmNames;
        const preprocessingSubMenuOptions = Array.from(new Set(
          preprocessingList
            .map(e => e.value.category)
            .filter(value => value !== undefined)
            .sort()
        ));
        const algorithmSubMenuOptions = Array.from(new Set(
          algorithmList
            .map(e => e.value.category)
            .filter(value => value !== undefined)
            .sort()
        ));
        let preprocessingMenu = [];
        if (preprocessingSubMenuOptions.length) {
          preprocessingSubMenuOptions.forEach(value => {
            preprocessingMenu.push({
              name: value,
              value: 'action',
              subMenus: preprocessingList.filter(e => e.value.category === value).sort(compare)
            });
          });
        } else {
          preprocessingMenu = preprocessingList;
        }
        let algorithmMenu = [];
        if (algorithmSubMenuOptions.length) {
          algorithmSubMenuOptions.forEach(value => {
            algorithmMenu.push({
              name: value,
              value: 'action',
              subMenus: algorithmList.filter(e => e.value.category === value).sort(compare)
            });
          });
        } else {
          algorithmMenu = algorithmList;
        }

        this.pipelinesMenu[0].subMenus = preprocessingMenu;
        this.pipelinesMenu[1].subMenus = algorithmMenu;
        this._cd.markForCheck();
      });

    this._store.pipe(select(fromWizard.getWorkflowHeaderData))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((data: any) => {
        this.workflowData.name = data.name;
        this.workflowData.version = data.version;
        this._cd.markForCheck();
      });

    // show create node pointer icon
    this._store.pipe(select(fromWizard.isCreationMode))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((creationMode: CreationMode) => {
        this.creationMode = creationMode;
        this._cd.markForCheck();
      });
    // show node editor fullscreen layout
    this._store.pipe(select(fromWizard.getEditionConfigMode))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((editionMode: any) => {
        this.editionConfigMode = editionMode || {};
        this._cd.markForCheck();
      });
    // show node/settings editor fullscreen layout
    this._store.pipe(select(fromWizard.showSettings))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((showSettings: boolean) => {
        this.showSettings = showSettings;
        this._cd.markForCheck();
      });
    // retrieves the environment list
    this._store.pipe(select(fromWizard.getParameters))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(parameters => {
        this.parameters = parameters;
        this._cd.markForCheck();
      });
    this._store.pipe(select(fromWizard.getEnvironmentList))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((environmentList: Array<any>) => {
        this.environmentList = environmentList;
        this._cd.markForCheck();
      });

    this._store.pipe(select(fromWizard.getMlModels))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((mlModelsList: Array<string>) => {
        this.mlModelsList = mlModelsList;
        this._cd.markForCheck();
      });
    this.showDebugConfig$ = this._store.pipe(select(fromWizard.isShowingDebugConfig));

    function compare(a, b) {
      if (a.label < b.label) {
        return -1;
      }
      if (a.label > b.label) {
        return 1;
      }
      return 0;
    }
  }

  weSavePipelinesWorkflow(event) {
    this._store.dispatch(new wizardActions.SaveEntityAction({
      oldName: event.name,
      data: event.data,
      closeEdition: false
    }));
    if (event.save) {
      (this._editor as any).saveWorkflow(false);
    }
  }

  closeCustomExecution() {
    this._store.dispatch(new debugActions.HideDebugConfigAction());
  }

  executeWorkflow(event) {
    this._store.dispatch(new debugActions.InitDebugWorkflowAction(event));
  }

  private _showCardsModal() {
    this._stModalService.container = this.target;
    this._stModalService.show({
      outputs: {
        closeCardsModal: this._onCloseCardsModal.bind(this)
      },
      maxWidth: 600
   }, WizardHelpCardsComponent);
  }

  private _onCloseCardsModal(close) {
    this._stModalService.close();
    if (close) {
      localStorage.setItem('sparta-cards-help', 'true');
    }
  }

  ngOnDestroy(): void {
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
    this._document.body.classList.remove('disable-scroll');
  }
}
