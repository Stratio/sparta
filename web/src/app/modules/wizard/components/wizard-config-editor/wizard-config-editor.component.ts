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
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { Store, select } from '@ngrx/store';
import { NgForm, FormGroup } from '@angular/forms';
import { StHorizontalTab } from '@stratio/egeo';
import { Router } from '@angular/router';
import { Subscription, Subject, Observable } from 'rxjs';

import { cloneDeep as _cloneDeep } from 'lodash';

import * as fromWizard from './../../reducers';
import * as wizardActions from './../../actions/wizard';
import { Environment } from '@models/environment';
import { ErrorMessagesService, InitializeSchemaService } from 'services';
import { WizardService } from '@app/wizard/services/wizard.service';
import { HelpOptions } from '@app/shared/components/sp-help/sp-help.component';
import { StepType } from 'app/models/enums';
import { EditionConfigMode } from '@app/wizard/models/node';

import * as fromQueryBuilder from '../query-builder/reducers';
import { take, takeUntil } from 'rxjs/operators';
import { WizardConfigEditorService } from './wizard-config.service';
import { writerOption, mockOption, globalOption } from './wizard-config.model';


@Component({
  selector: 'wizard-config-editor',
  styleUrls: ['wizard-config-editor.styles.scss'],
  templateUrl: 'wizard-config-editor.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardConfigEditorComponent implements OnInit, OnDestroy {

  @Input() config: EditionConfigMode;
  @Input() workflowType: string;
  @Input() parameters: any;
  @Input() environmentList: Array<Environment> = [];
  @Input() mlModelsList: Array<string> = [];
  @Input() nodeWriters: any;
  @ViewChild('entityForm') public entityForm: NgForm;

  public basicSettings: any = [];
  public writerSettings: any = [];
  public submitted = true;
  public currentName = '';
  public arity: any;
  public isShowedHelp = false;
  public valueDictionary: any = {};

  public isTemplate = false;
  public templateData: any;

  public validatedName = false;
  public basicFormModel: any = {};    // inputs, outputs, transformation basic settings (name, description)
  public entityFormModel: any = {};   // common config
  public showCrossdataCatalog = false;
  public isShowedInfo = true;
  public fadeActive = false;

  public activeOption: StHorizontalTab;
  public options: StHorizontalTab[] = [];
  public debugOptions: any = {};
  public helpOptions: Array<HelpOptions> = [];
  public editTitle = false;
  public queryBuilder: any;
  public visualQueryBuilder = false;
  public nodeWritersNames$: Observable<any>;
  public errorTableName = '';
  public engine$: Observable<string>;

  public writersGroup = new FormGroup({});
  public validatedName$: Observable<boolean>;
  private _componentDestroyed = new Subject();
  private saveSubscription: Subscription;

  constructor(
    private _store: Store<fromWizard.State>,
    private _router: Router,
    private _initializeSchemaService: InitializeSchemaService,
    private _cd: ChangeDetectorRef,
    private _wizardService: WizardService,
    private _wizardConfigEditorService: WizardConfigEditorService,
    public errorsService: ErrorMessagesService
  ) { }

  public ngOnInit(): void {
    setTimeout(() => {
      this.fadeActive = true;
      this._cd.markForCheck();
    });
    this.valueDictionary.parameters = this.parameters;
    if (this.config.schemas && this.config.schemas.inputs && this.config.schemas.inputs.length) {
      let attrs = [];
      this.config.schemas.inputs.forEach(input => attrs = attrs.concat(this._getInputSchema(input)));
      this.valueDictionary.formFieldsVariables = [...attrs];
      this.valueDictionary.formFieldsVariables.sort((a, b) => (a.name.toLowerCase() > b.name.toLowerCase()) ? 1 : ((b.name.toLowerCase() > a.name.toLowerCase()) ? -1 : 0));
    }

    if (this.config.inputSteps && this.config.inputSteps.length) {
      this.valueDictionary.inputStepsVariables = [...this.config.inputSteps.map(step => ({
        name: step,
        value: step,
        valueType: 'step'
      }))];
      this.valueDictionary.inputStepsVariables.sort((a, b) => (a.name.toLowerCase() > b.name.toLowerCase()) ? 1 : ((b.name.toLowerCase() > a.name.toLowerCase()) ? -1 : 0));
    }
    this.valueDictionary.mlModelsList = this.mlModelsList.map(model => ({
      label: model,
      value: model
    }));
    this._getMenuTabs();
    this.validatedName$ = this._store.pipe(select(fromWizard.getValidatedEntityName));
    this.engine$ = this._store.pipe(select(fromWizard.getWorkflowType));
    this.saveSubscription = this._store.pipe(select(fromWizard.isEntitySaved))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((isEntitySaved) => {
        if (isEntitySaved) {
          // hide edition when its saved
          this._store.dispatch(new wizardActions.HideEditorConfigAction());
        }
      });
    this._store.pipe(select(fromWizard.isShowedCrossdataCatalog))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((showed: boolean) => {
        this.isShowedInfo = showed;
        this._cd.markForCheck();
      });

    this._store.pipe(select(fromQueryBuilder.getQueryBuilderInnerState))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((queryBuilder: any) => {
        this.queryBuilder = queryBuilder;
        this._cd.markForCheck();
      });

    this.getFormTemplate();
    this.visualQueryBuilder = true;
    if (this.nodeWriters) {
      this._getWriterOutputNames();
    }
  }

  public ngOnDestroy(): void {
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
    const isPipelineNodeEdition = (this.config.editionType.data.classPrettyName === 'MlPipeline');
    this._store.dispatch(new wizardActions.ModifyIsPipelinesNodeEdition(isPipelineNodeEdition));
    if (this.saveSubscription) {
      this.saveSubscription.unsubscribe();
    }
  }

  public resetValidation() {
    this._store.dispatch(new wizardActions.SaveEntityErrorAction(false));
  }

  public cancelEdition() {
    this._store.dispatch(new wizardActions.HideEditorConfigAction());
    this._store.dispatch(new wizardActions.SaveEntityErrorAction(false));
  }

  public changeFormOption(event: StHorizontalTab) {
    this.activeOption = event;
    if (event.id === 'Global') {
      this.helpOptions = this._initializeSchemaService.getHelpOptions(this.basicSettings);
    } else if (event.id === 'Writer') {
      this.helpOptions = this._initializeSchemaService.getHelpOptions(this.writerSettings);
    }
  }

  public onEditTitle() {
    this.editTitle = true;
  }

  public onSaveName() {
    this.editTitle = false;
  }

  public toggleInfo() {
    this._store.dispatch(new wizardActions.ToggleCrossdataCatalogAction());
  }

  public editTemplate(templateId) {
    let routeType = '';
    switch (this.config.editionType.data.stepType) {
      case StepType.Input:
        routeType = 'inputs';
        break;
      case StepType.Output:
        routeType = 'outputs';
        break;
      case StepType.Transformation:
        routeType = 'transformations';
        break;
      default:
        return;
    }
    const ask = window.confirm('If you leave this page you will lose the unsaved changes of the workflow');
    if (ask) {
      this._router.navigate(['templates', routeType, 'edit', templateId]);
    }
  }

  public closeHelp() {
    this.isShowedHelp = false;
  }

  public getFormTemplate() {
    if (this.config.editionType.data.createdNew) {
      this.submitted = false;
    }
    this.entityFormModel = _cloneDeep(this.config.editionType.data);
    const debugOptions = this.entityFormModel.configuration.debugOptions || {};
    this.debugOptions = typeof debugOptions === 'string' ? JSON.parse(debugOptions) : debugOptions;
    this.currentName = this.entityFormModel['name'];
    let template: any;
    switch (this.config.editionType.stepType) {
      case StepType.Input:
        template = this._wizardService.getInputs()[this.config.editionType.data.classPrettyName];
        break;
      case StepType.Output:
        template = this._wizardService.getOutputs()[this.config.editionType.data.classPrettyName];
        break;
      case StepType.Transformation:
        template = this._wizardService.getTransformations()[this.config.editionType.data.classPrettyName];
        break;
    }
    this.helpOptions = this._initializeSchemaService.getHelpOptions(template.properties);
    this._cd.markForCheck();
    this.showCrossdataCatalog = !!(template.crossdataCatalog);
    this.basicSettings = template.properties;
    this.errorTableName = this.entityFormModel.configuration.errorTableName || '';
    if (this.entityFormModel.nodeTemplate && this.entityFormModel.nodeTemplate.id && this.entityFormModel.nodeTemplate.id.length) {
      this.isTemplate = true;
      const nodeTemplate = this.entityFormModel.nodeTemplate;
      this._store.pipe(select(fromWizard.getTemplates)).pipe(take(1)).subscribe((templates: any) => {
        this.templateData = templates[this.config.editionType.stepType.toLowerCase()]
          .find((templateD: any) => templateD.id === nodeTemplate.id);
      });
    } else {
      this.isTemplate = false;
      if (template.arity) {
        this.arity = template.arity;
      }
    }
  }

  public saveForm() {
    this.entityFormModel.hasErrors = this.entityForm.invalid;
    if (this.arity) {
      this.entityFormModel.arity = this.arity;
    }
    if (this.isTemplate) {
      this.entityFormModel.configuration = this.templateData.configuration;
    }

    if (this.debugOptions.selectedMock) {
      const value = this.debugOptions[this.debugOptions.selectedMock];
      if (value && value.length) {
        this.entityFormModel.configuration.debugOptions = {};
        this.entityFormModel.configuration.debugOptions[this.debugOptions.selectedMock] = value;
      }
    }
    this.entityFormModel.relationType = this.config.editionType.data.relationType;
    this.entityFormModel.createdNew = false;
    this.entityFormModel.configuration.errorTableName = this.errorTableName;
    if (this.entityFormModel.classPrettyName === 'QueryBuilder') {
      const queryConfiguration = this._wizardConfigEditorService.normalizeQueryConfiguration(this.queryBuilder);
      this.queryBuilder.outputSchemaFields = this.queryBuilder.outputSchemaFields.map(output => ({
        ...output,
        position: { ...output.position, y: output.position.y > 200 ? output.position.y - 110 : output.position.y }
      }));

      const queryEntityFormModel = {
        ...this.entityFormModel,
        configuration: {
          ...this.entityFormModel.configuration,
          visualQuery: queryConfiguration,
          backup: this.queryBuilder
        },
        uiConfiguration: {
          ...this.entityFormModel.uiConfiguration,
          backup: this.queryBuilder
        }
      };
      this._store.dispatch(new wizardActions.SaveEntityAction({
        oldName: this.config.editionType.data.name,
        data: queryEntityFormModel
      }));

    } else {
      this._store.dispatch(new wizardActions.SaveEntityAction({
        oldName: this.config.editionType.data.name,
        data: this.entityFormModel,
        closeEdition: true
      }));
    }
    // save writer options
    if (this.nodeWriters) {
      this._store.dispatch(new wizardActions.SaveNodeWriterOptions(this.entityFormModel.id, this.writersGroup.value));
    }
  }

  private _getMenuTabs() {
    const options: StHorizontalTab[] = [globalOption];
    if (this.nodeWriters) {
      options.push(this._getWriterTab(this.writersGroup.valid));
      this.writersGroup.statusChanges
        .pipe(takeUntil(this._componentDestroyed))
        .subscribe((status) => {
          const writerTab = this._getWriterTab(this.writersGroup.valid);
          this.options = this.options.map(option => option.id === writerOption.id ?
            writerTab : option);
          this._cd.markForCheck();
        });
    }
    if (this.config.editionType.stepType === StepType.Input) {
      options.push(mockOption);
    }
    this.options = options;
    this.activeOption = options[0];
  }

  private _getWriterTab(status: boolean) {
    return {
      ...writerOption,
      text: writerOption.text + ` (${Object.keys(this.nodeWriters).length})`
    };
  }

  private _getInputSchema(input: any) {
    if (input.result && input.result.schema && input.result.schema.fields) {
      return input.result.schema.fields.map(field => ({
        name: field.name,
        value: field.name,
        inputName: input.result.step,
        valueType: 'field'
      }));
    } else {
      return [];
    }
  }

  private _getWriterOutputNames() {
    this.nodeWritersNames$ = this._store.select(fromWizard.getStepNamesFromIDs(Object.keys(this.nodeWriters)));
  }
}

