/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Subject } from 'rxjs/Subject';

import * as fromWizard from './reducers';
import * as debugActions from './actions/debug';
import * as wizardActions from './actions/wizard';
import * as externalDataActions from './actions/externalData';
import { WizardService } from './services/wizard.service';
import { Engine } from 'app/models/enums';
import { CreationMode, EditionConfigMode } from './models/node';

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

   private _componentDestroyed = new Subject();

   constructor(
      private _store: Store<fromWizard.State>,
      private _route: ActivatedRoute,
      private _cd: ChangeDetectorRef,
      private _wizardService: WizardService) { }


   ngOnInit(): void {
      const id = this._route.snapshot.params.id;
      this.isEdit = id && id.length ? true :  false;
      this._store.dispatch(new externalDataActions.GetEnvironmentListAction());
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
      // Retrieves the workflow type from store (in edition mode, is updated after the get workflow data request)
      this._store.select(fromWizard.getWorkflowType)
         .takeUntil(this._componentDestroyed)
         .subscribe((workflowType: string) => {
            this._wizardService.workflowType = workflowType;
            this.workflowType = workflowType;
         });
      // show create node pointer icon
      this._store.select(fromWizard.isCreationMode)
         .takeUntil(this._componentDestroyed)
         .subscribe((creationMode: CreationMode) => {
            this.creationMode = creationMode;
            this._cd.markForCheck();
         });
      // show node editor fullscreen layout
      this._store.select(fromWizard.getEditionConfigMode)
         .takeUntil(this._componentDestroyed)
         .subscribe(editionMode => {
            this.editionConfigMode = editionMode;
            this._cd.markForCheck();
         });
      // show node/settings editor fullscreen layout
      this._store.select(fromWizard.showSettings)
         .takeUntil(this._componentDestroyed)
         .subscribe(showSettings => {
            this.showSettings = showSettings;
            this._cd.markForCheck();
         });
      // retrieves the environment list
      this._store.select(fromWizard.getEnvironmentList)
         .takeUntil(this._componentDestroyed)
         .subscribe((environmentList: Array<any>) => {
            this.environmentList = environmentList;
            this._cd.markForCheck();
         });
   }

   ngOnDestroy(): void {
      this._componentDestroyed.next();
      this._componentDestroyed.unsubscribe();
   }
}
