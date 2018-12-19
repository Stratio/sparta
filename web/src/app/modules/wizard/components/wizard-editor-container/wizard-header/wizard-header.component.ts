/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Location } from '@angular/common';
import { Component, OnInit, OnDestroy, ChangeDetectorRef, Output, EventEmitter, Input, ViewChild, ViewContainerRef } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { Store, select } from '@ngrx/store';

import { StModalService } from '@stratio/egeo';

import * as fromWizard from './../../../reducers';
import * as wizardActions from './../../../actions/wizard';
import * as debugActions from './../../../actions/debug';

import { WizardModalComponent } from './../../wizard-modal/wizard-modal.component';
import { MenuOptionListGroup } from '@app/shared/components/menu-options-list/menu-options-list.component';
import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.model';
import { Observable, Subject } from 'rxjs';
import { distinctUntilChanged, takeUntil } from 'rxjs/operators';

import { WorkflowData } from '@app/wizard/models/data';

@Component({
   selector: 'wizard-header',
   styleUrls: ['wizard-header.styles.scss'],
   templateUrl: 'wizard-header.template.html'
})

export class WizardHeaderComponent implements OnInit, OnDestroy {
   @Input() selectedNodeNumber = 0;
   @Input() isEdgeSelected = false;
   @Input() isPipelinesNodeSelected: boolean;
   @Input() isWorkflowDebugging: boolean;
   @Input() workflowData: WorkflowData;
   @Input() multiselectionMode: boolean;
   @Input() currentZoom: any = {};

   @Output() onZoomIn = new EventEmitter();
   @Output() onZoomOut = new EventEmitter();
   @Output() onCenter = new EventEmitter();
   @Output() setZoom = new EventEmitter<number>();
   @Output() onDelete = new EventEmitter();
   @Output() onSaveWorkflow = new EventEmitter<boolean>();
   @Output() onEditEntity = new EventEmitter();
   @Output() deleteSelection = new EventEmitter();
   @Output() onEditPipelinesEntity = new EventEmitter();

   @ViewChild('nameForm') public nameForm: NgForm;
   @ViewChild('wizardModal', { read: ViewContainerRef }) target: any;

   public runOptions: MenuOptionListGroup[] = [
      {
         options: [
            {
               label: 'Debug',
               id: 'simple'
            },
            {
               label: 'Debug with parameters',
               id: 'advanced'
            }
         ]
      }
   ];
   public isShowedEntityDetails$: Observable<boolean>;
   public menuOptions$: Observable<Array<FloatingMenuModel>>;
   public isLoading$: Observable<boolean>;

   public undoEnabled = false;
   public redoEnabled = false;
   public isPristine = true;
   public genericError: any;
   public validations: any = {};

   private _componentDestroyed = new Subject();

   constructor(private route: Router,
      private _store: Store<fromWizard.State>,
      private _cd: ChangeDetectorRef,
      private _modalService: StModalService,
      private _location: Location) { }

   ngOnInit(): void {
      this.isShowedEntityDetails$ = this._store.pipe(select(fromWizard.isShowedEntityDetails))
        .pipe(distinctUntilChanged());
      this.isLoading$ = this._store.pipe(select(fromWizard.isLoading));

      this._store.pipe(select(fromWizard.areUndoRedoEnabled))
         .pipe(takeUntil(this._componentDestroyed))
         .subscribe((actions: any) => {
            this.undoEnabled = actions.undo;
            this.redoEnabled = actions.redo;
            this._cd.markForCheck();
         });

      this._store.pipe(select(fromWizard.getValidationErrors))
         .pipe(takeUntil(this._componentDestroyed))
         .subscribe((validations: any) => {
            this.validations = validations;
            this._cd.markForCheck();
         });

      this._store.pipe(select(fromWizard.isPristine)).pipe(distinctUntilChanged())
         .pipe(takeUntil(this._componentDestroyed))
         .subscribe((isPristine: boolean) => {
            this.isPristine = isPristine;
            this._cd.markForCheck();
         });

      this._store.pipe(select(fromWizard.getDebugResult))
         .pipe(takeUntil(this._componentDestroyed))
         .subscribe((debugResult: any) => {
            this.genericError = debugResult && debugResult.genericError ? debugResult.genericError : null;
            this._cd.markForCheck();
         });

      this.menuOptions$ = this._store.pipe(select(fromWizard.getMenuOptions));
   }

   selectedMenuOption($event: any): void {
      this._store.dispatch(new wizardActions.SelectedCreationEntityAction($event));
   }

   showSettings() {
      this._store.dispatch(new wizardActions.ShowSettingsAction());
   }

   filterOptions($event: any) {
      this._store.dispatch(new wizardActions.SearchFloatingMenuAction($event));
   }

   toggleEntityInfo() {
      this._store.dispatch(new wizardActions.ToggleDetailSidebarAction());
   }

   debugWorkflow() {
      this._store.dispatch(new debugActions.InitDebugWorkflowAction());
   }

   showGlobalErrors() {
      this._store.dispatch(new wizardActions.ShowGlobalErrorsAction());
   }

   selectedDebug(event) {
      if (event === 'simple') {
         this.debugWorkflow();
      } else {
         this._store.dispatch(new debugActions.ShowDebugConfigAction());
      }
   }

   public showConfirmModal(): void {
      if (this.isPristine) {
         this.redirectPrevious();
         return;
      }
      this._modalService.container = this.target;
      this._modalService.show({
         modalTitle: 'You will lose your changes',
         outputs: {
            onCloseConfirmModal: this.onCloseConfirmationModal.bind(this)
         },
         maxWidth: 600
      }, WizardModalComponent);
   }

   public saveWorkflow(): void {
      this.onSaveWorkflow.emit();
   }

   onCloseConfirmationModal(event: any) {
      this._modalService.close();
      if (event === '0') {
         this.onSaveWorkflow.emit(true);
      } else if (event === '1') {
         this.redirectPrevious();
      }
   }

   redirectPrevious() {
      if (window.history.length > 2) {
         this._location.back();
      } else {
         this.route.navigate(['repository']);
      }
   }

   undoAction() {
      this._store.dispatch(new wizardActions.UndoChangesAction());
   }

   redoAction() {
      this._store.dispatch(new wizardActions.RedoChangesAction());
   }

   ngOnDestroy(): void {
      this._componentDestroyed.next();
      this._componentDestroyed.unsubscribe();
   }
}
