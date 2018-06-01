/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
   Component, OnInit, OnDestroy, ChangeDetectionStrategy, Input, ViewChild, ChangeDetectorRef
} from '@angular/core';
import { Store } from '@ngrx/store';
import { NgForm } from '@angular/forms';
import { StHorizontalTab } from '@stratio/egeo';
import { Router } from '@angular/router';
import { Subject } from 'rxjs/Subject';
import { cloneDeep as _cloneDeep } from 'lodash';
import * as fromWizard from './../../reducers';
import * as wizardActions from './../../actions/wizard';

import { Subscription } from 'rxjs/Subscription';
import { ErrorMessagesService, InitializeSchemaService } from 'services';
import { writerTemplate } from 'data-templates/index';
import { WizardService } from '@app/wizard/services/wizard.service';
import { HelpOptions } from '@app/shared/components/sp-help/sp-help.component';

@Component({
   selector: 'wizard-config-editor',
   styleUrls: ['wizard-config-editor.styles.scss'],
   templateUrl: 'wizard-config-editor.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardConfigEditorComponent implements OnInit, OnDestroy {

   @Input() config: any;
   @Input() workflowType: string;
   @ViewChild('entityForm') public entityForm: NgForm;

   public basicSettings: any = [];
   public writerSettings: any = [];
   public submitted = true;
   public currentName = '';
   public arity: any;
   public isShowedHelp = false;

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
      });
      this._getMenuTabs();
      this.validatedNameSubcription = this._store.select(fromWizard.getValidatedEntityName)
         .takeUntil(this._componentDestroyed)
         .subscribe((validation: boolean) => {
            this.validatedName = validation;
         });

      this.saveSubscription = this._store.select(fromWizard.isEntitySaved)
         .takeUntil(this._componentDestroyed)
         .subscribe((isEntitySaved) => {
            if (isEntitySaved) {
               // hide edition when its saved
               this._store.dispatch(new wizardActions.HideEditorConfigAction());
            }
         });
      this._store.select(fromWizard.isShowedCrossdataCatalog)
         .takeUntil(this._componentDestroyed)
         .subscribe((showed: boolean) => {
            this.isShowedInfo = showed;
            this._cd.markForCheck();
         });

      this.getFormTemplate();
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

   toggleInfo() {
      this._store.dispatch(new wizardActions.ToggleCrossdataCatalogAction());
   }

   editTemplate(templateId) {
      let routeType = '';
      switch (this.config.editionType.data.stepType) {
         case 'Input':
            routeType = 'inputs';
            break;
         case 'Output':
            routeType = 'outputs';
            break;
         case 'Transformation':
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
         case 'Input':
            template = this._wizardService.getInputs()[this.config.editionType.data.classPrettyName];
            this.writerSettings = writerTemplate;
            break;
         case 'Output':
            template = this._wizardService.getOutputs()[this.config.editionType.data.classPrettyName];
            break;
         case 'Transformation':
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
         this._store.select(fromWizard.getTemplates).take(1).subscribe((templates: any) => {
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
      this._store.dispatch(new wizardActions.SaveEntityAction({
         oldName: this.config.editionType.data.name,
         data: this.entityFormModel
      }));
   }

   _getMenuTabs() {
      switch (this.config.editionType.stepType) {
         case 'Input':
            this.options = this._allOptions;
            break;
         case 'Transformation':
            this.options = this._allOptions.slice(0, 2);
            break;
         case 'Output':
            this.options = [];
            break;
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
