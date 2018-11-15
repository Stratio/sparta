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
import { Store } from '@ngrx/store';
import { NgForm } from '@angular/forms';
import { StHorizontalTab } from '@stratio/egeo';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';

import { cloneDeep as _cloneDeep } from 'lodash';
import * as fromWizard from './../../reducers';
import * as wizardActions from './../../actions/wizard';
import { Environment } from '../../../../models/environment';

import { Subscription } from 'rxjs/Subscription';
import { ErrorMessagesService, InitializeSchemaService } from 'services';
import { writerTemplate } from 'data-templates/index';
import { WizardService } from '@app/wizard/services/wizard.service';
import { HelpOptions } from '@app/shared/components/sp-help/sp-help.component';
import { StepType } from 'app/models/enums';

import * as fromQueryBuilder from '../query-builder/reducers';


@Component({
   selector: 'wizard-config-editor',
   styleUrls: ['wizard-config-editor.styles.scss'],
   templateUrl: 'wizard-config-editor.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardConfigEditorComponent implements OnInit, OnDestroy {

   @Input() config: any;
   @Input() workflowType: string;
   @Input() parameters;
   @Input() environmentList: Array<Environment> = [];
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

   public activeOption = 'Global';
   public options: StHorizontalTab[] = [];
   public debugOptions: any = {};
   public helpOptions: Array<HelpOptions> = [];
   public editTitle = false;
   public queryBuilder: any;
   public visualQueryBuilder = false;

   private _componentDestroyed = new Subject();
   private _allOptions: StHorizontalTab[] = [{
      id: 'Global',
      text: 'Global'
   }, {
      id: 'Writer',
      text: 'Writer'
   },
   {
      id: 'Mocks',
      text: 'Mock Data'
   }];
   private saveSubscription: Subscription;
   private validatedNameSubcription: Subscription;

   ngOnInit(): void {
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
      this._getMenuTabs();
      this.validatedNameSubcription = this._store.select(fromWizard.getValidatedEntityName)
         .pipe(takeUntil(this._componentDestroyed))
         .subscribe((validation: boolean) => {
            this.validatedName = validation;
            this._cd.markForCheck();
         });

      this.saveSubscription = this._store.select(fromWizard.isEntitySaved)
         .pipe(takeUntil(this._componentDestroyed))
         .subscribe((isEntitySaved) => {
            if (isEntitySaved) {
               // hide edition when its saved
               this._store.dispatch(new wizardActions.HideEditorConfigAction());
            }
         });
      this._store.select(fromWizard.isShowedCrossdataCatalog)
         .pipe(takeUntil(this._componentDestroyed))
         .subscribe((showed: boolean) => {
            this.isShowedInfo = showed;
            this._cd.markForCheck();
         });

      this._store.select(fromQueryBuilder.getQueryBuilderInnerState)
         .pipe(takeUntil(this._componentDestroyed))
         .subscribe((queryBuilder: any) => {
            this.queryBuilder = queryBuilder;
            this._cd.markForCheck();
         });

      this.getFormTemplate();
      this.visualQueryBuilder = true;
   }

   resetValidation() {
      this._store.dispatch(new wizardActions.SaveEntityErrorAction(false));
   }

   cancelEdition() {
      this._store.dispatch(new wizardActions.HideEditorConfigAction());
      this._store.dispatch(new wizardActions.SaveEntityErrorAction(false));
   }

   changeFormOption(event: any) {
      this.activeOption = event.id;
      if (event.id === 'Global') {
         this.helpOptions = this._initializeSchemaService.getHelpOptions(this.basicSettings);
      } else if (event.id === 'Writer') {
         this.helpOptions = this._initializeSchemaService.getHelpOptions(this.writerSettings);
      }
   }
   onEditTitle() {
      this.editTitle = true;
   }

   onSaveName(event) {
      this.editTitle = false;
   }

   toggleInfo() {
      this._store.dispatch(new wizardActions.ToggleCrossdataCatalogAction());
   }

   editTemplate(templateId) {
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

   closeHelp() {
      this.isShowedHelp = false;
   }

   getFormTemplate() {
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
            this.writerSettings = writerTemplate;
            break;
         case StepType.Output:
            template = this._wizardService.getOutputs()[this.config.editionType.data.classPrettyName];
            break;
         case StepType.Transformation:
            template = this._wizardService.getTransformations()[this.config.editionType.data.classPrettyName];
            this.writerSettings = writerTemplate;
            break;
      }
      this.helpOptions = this._initializeSchemaService.getHelpOptions(template.properties);
      this._cd.markForCheck();
      this.showCrossdataCatalog = template.crossdataCatalog ? true : false;
      this.basicSettings = template.properties;
      if (this.entityFormModel.nodeTemplate && this.entityFormModel.nodeTemplate.id && this.entityFormModel.nodeTemplate.id.length) {
         this.isTemplate = true;
         const nodeTemplate = this.entityFormModel.nodeTemplate;
         this._store.select(fromWizard.getTemplates).pipe(take(1)).subscribe((templates: any) => {
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
      if (this.entityFormModel.classPrettyName === 'QueryBuilder') {
         const queryConfiguration = this.normalizeQueryConfiguration();
         this.queryBuilder.outputSchemaFields = this.queryBuilder.outputSchemaFields.map(output => ({
            ...output,
            position: { ...output.position, y: output.position.y > 200 ? output.position.y - 110 : output.position.y }
         }));

         const queryEntityFormModel = {
            ...this.entityFormModel,
            configuration: { ...this.entityFormModel.configuration, visualQuery: queryConfiguration, backup: this.queryBuilder },
            uiConfiguration: { ...this.entityFormModel.uiConfiguration, backup: this.queryBuilder }
         };
         this._store.dispatch(new wizardActions.SaveEntityAction({
            oldName: this.config.editionType.data.name,
            data: queryEntityFormModel
         }));

      } else {
         this._store.dispatch(new wizardActions.SaveEntityAction({
            oldName: this.config.editionType.data.name,
            data: this.entityFormModel
         }));
      }

   }

   normalizeQueryConfiguration() {
      const inputs = this.queryBuilder.inputSchemaFields.length;
      const usedInputs = [].concat.apply([], this.queryBuilder.outputSchemaFields
         .map(output => output.originFields.map(field => `${field.alias}.${field.table}`)))
         .filter((elem, index, self) => index === self.indexOf(elem));
      let joinClause = null;
      let fromClause = null;
      if (usedInputs.length > 1 ||  this.queryBuilder.join.type.includes('RIGHT_ONLY') ||  this.queryBuilder.join.type.includes('LEFT_ONLY')) {
         // JOIN
         if (this.queryBuilder.join && this.queryBuilder.join.joins && this.queryBuilder.join.joins.length) {
               const origin = this.queryBuilder.join.joins[0].origin;
               const destination = this.queryBuilder.join.joins[0].destination;
               const leftTable = { tableName: origin.table, alias: origin.alias };
               const rightTable = { tableName: destination.table, alias: destination.alias };
               const joinTypes = this.queryBuilder.join.type;
               const joinConditions = this.queryBuilder.join.joins.map(join => ({ leftField: join.origin.column, rightField: join.destination.column }));
               joinClause = { leftTable, rightTable, joinTypes, joinConditions };
         } else {
            joinClause = {
               leftTable: { tableName: this.queryBuilder.inputSchemaFields[0].name, alias:  this.queryBuilder.inputSchemaFields[0].alias },
               rightTable: { tableName: this.queryBuilder.inputSchemaFields[1].name, alias:  this.queryBuilder.inputSchemaFields[1].alias },
               joinTypes: 'CROSS'
            };
         }
      } else {
         // FROM
         if (this.queryBuilder.inputSchemaFields.length || this.queryBuilder.outputSchemaFields.length) {
            const table = this.queryBuilder.inputSchemaFields[0];
            const ouputTable = this.queryBuilder.outputSchemaFields[0] || [];
            fromClause = {
               tableName: ouputTable.originFields && ouputTable.originFields.length ? ouputTable.originFields[0].table : table.name,
               alias: ouputTable.originFields && ouputTable.originFields.length ? ouputTable.originFields[0].alias : table.alias
            };
         }
      }



      // SELECT
      const selectClauses = this.queryBuilder.outputSchemaFields
         .map(output => {
            if (output.column) {
               return  {
                  expression: output.expression,
                  alias: output.column
               };
            } else {
               return  {
                  expression: output.expression
               };
            }
         });

      // WHERE
      const whereClause = this.queryBuilder.filter;



      // ORDERBY
      const orderByClauses = this.queryBuilder.outputSchemaFields
         .map((output, position) => {
            const order = !output.order ? output.order : output.order === 'orderAsc' ? 'ASC' : 'DESC';
            return { field: output.expression , order , position };
         })
         .filter(output => !!output.order);


      const result = { selectClauses, whereClause, joinClause, orderByClauses, fromClause };
      // Delete null references
      Object.keys(result).forEach((key) => (result[key] === null) && delete result[key]);

      return result;
   }

   private _getMenuTabs() {
      switch (this.config.editionType.stepType) {
         case StepType.Input:
            this.options = this._allOptions;
            break;
         case StepType.Transformation:
            this.options = this._allOptions.slice(0, 2);
            break;
         case StepType.Output:
            this.options = [];
            break;
      }
   }

   private _getInputSchema(input: any) {
      if (input.result && input.result.schema && input.result.schema.fields) {
         return input.result.schema.fields.map(field => ({
            name: field.name,
            value: field.name,
            valueType: 'field'
         }));
      } else {
         return [];
      }
   }

   constructor(private _store: Store<fromWizard.State>,
      private _router: Router,
      private _initializeSchemaService: InitializeSchemaService,
      private _cd: ChangeDetectorRef,
      private _wizardService: WizardService,
      public errorsService: ErrorMessagesService) {
   }

   ngOnDestroy(): void {
      this._componentDestroyed.next();
      this._componentDestroyed.unsubscribe();
   }
}

