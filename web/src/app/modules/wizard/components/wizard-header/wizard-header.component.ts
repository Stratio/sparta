/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Location } from '@angular/common';
import { Component, OnInit, OnDestroy, ChangeDetectorRef, Output, EventEmitter, Input, ViewChild, ViewContainerRef } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';

import 'rxjs/add/operator/distinctUntilChanged';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';

import { StModalService } from '@stratio/egeo';

import * as fromWizard from './../../reducers';
import * as wizardActions from './../../actions/wizard';

import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';
import { WizardModalComponent } from './../wizard-modal/wizard-modal.component';
import { workflowNamePattern } from './../../wizard.constants';

@Component({
   selector: 'wizard-header',
   styleUrls: ['wizard-header.styles.scss'],
   templateUrl: 'wizard-header.template.html'
})

export class WizardHeaderComponent implements OnInit, OnDestroy {

   @Output() onZoomIn = new EventEmitter();
   @Output() onZoomOut = new EventEmitter();
   @Output() onCenter = new EventEmitter();
   @Output() onDelete = new EventEmitter();
   @Output() onSaveWorkflow = new EventEmitter<boolean>();
   @Output() onEditEntity = new EventEmitter();
   @Output() deleteSelection = new EventEmitter();
   @Output() onDuplicateNode = new EventEmitter();

   @Input() isNodeSelected = false;
   @Input() isEdgeSelected = false;
   @Input() workflowName = '';
   @Input() workflowVersion = 0;

   @ViewChild('nameForm') public nameForm: NgForm;
   @ViewChild('wizardModal', { read: ViewContainerRef }) target: any;

   public workflowNamePattern = workflowNamePattern;
   public isShowedEntityDetails$: Observable<boolean>;
   public menuOptions$: Observable<Array<FloatingMenuModel>>;
   public isLoading$: Observable<boolean>;
   public showErrors = false;
   public workflowType = '';

   public undoEnabled = false;
   public redoEnabled = false;
   public isPristine = true;
   public validations: any = {};

   private _componentDestroyed = new Subject();

   constructor(private route: Router,
      private currentActivatedRoute: ActivatedRoute,
      private store: Store<fromWizard.State>,
      private _cd: ChangeDetectorRef,
      private _modalService: StModalService,
      private _location: Location) { }

   ngOnInit(): void {
      this._modalService.container = this.target;
      this.isShowedEntityDetails$ = this.store.select(fromWizard.isShowedEntityDetails).distinctUntilChanged();
      this.isLoading$ = this.store.select(fromWizard.isLoading);

      this.store.select(fromWizard.areUndoRedoEnabled)
         .takeUntil(this._componentDestroyed)
         .subscribe((actions: any) => {
            this.undoEnabled = actions.undo;
            this.redoEnabled = actions.redo;
            this._cd.markForCheck();
         });

      this.store.select(fromWizard.getValidationErrors)
         .takeUntil(this._componentDestroyed)
         .subscribe((validations: any) => {
            this.validations = validations;
            this._cd.markForCheck();
         });

      this.store.select(fromWizard.isPristine).distinctUntilChanged()
         .takeUntil(this._componentDestroyed)
         .subscribe((isPristine: boolean) => {
            this.isPristine = isPristine;
            this._cd.markForCheck();
         });

      this.store.select(fromWizard.getWorkflowType)
         .takeUntil(this._componentDestroyed)
         .subscribe((type) => this.workflowType = type);

      this.menuOptions$ = this.store.select(fromWizard.getMenuOptions);
   }

   selectedMenuOption($event: any): void {
      this.store.dispatch(new wizardActions.SelectedCreationEntityAction($event));
   }

   showSettings() {
      this.store.dispatch(new wizardActions.ShowSettingsAction());
   }

   filterOptions($event: any) {
      this.store.dispatch(new wizardActions.SearchFloatingMenuAction($event));
   }

   toggleEntityInfo() {
      this.store.dispatch(new wizardActions.ToggleDetailSidebarAction());
   }

   public showConfirmModal(): void {
      if (this.isPristine) {
         this.redirectPrevious();
         return;
      }
      this._modalService.show({
         modalTitle: 'Exit workflow',
         outputs: {
            onCloseConfirmModal: this.onCloseConfirmationModal.bind(this)
         }
      }, WizardModalComponent);
   }

   public saveWorkflow(): void {
      this.onSaveWorkflow.emit();
   }

   onCloseConfirmationModal(event: any) {
      this._modalService.close();
      if (event === '1') {
         this.onSaveWorkflow.emit(true);
      } else {
         this.redirectPrevious();
      }
   }

   redirectPrevious() {
      if (window.history.length > 2) {
         this._location.back();
      } else {
         this.route.navigate(['workflow-managing']);
      }
   }

   undoAction() {
      this.store.dispatch(new wizardActions.UndoChangesAction());
   }

   redoAction() {
      this.store.dispatch(new wizardActions.RedoChangesAction());
   }

   ngOnDestroy(): void {
      this._componentDestroyed.next();
      this._componentDestroyed.unsubscribe();
   }
}
