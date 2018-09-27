/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Location } from '@angular/common';
import { Component, OnInit, OnDestroy, ChangeDetectorRef, Output, EventEmitter, Input, ViewChild, ViewContainerRef } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';

import 'rxjs/add/operator/takeUntil';
import 'rxjs/add/operator/distinctUntilChanged';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';

import { StModalService } from '@stratio/egeo';

import * as fromWizard from './../../reducers';
import * as wizardActions from './../../actions/wizard';
import * as debugActions from './../../actions/debug';

import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';
import { WizardModalComponent } from './../wizard-modal/wizard-modal.component';
import { MenuOptionListGroup } from "@app/shared/components/menu-options-list/menu-options-list.component";

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
  @Input() isWorkflowDebugging: boolean;
  @Input() workflowVersion = 0;

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
               label: 'Debug with custom params',
               id: 'advanced'
            }
         ]
      }
   ];
  public isShowedEntityDetails$: Observable<boolean>;
  public menuOptions$: Observable<Array<FloatingMenuModel>>;
  public isLoading$: Observable<boolean>;
  public workflowType = '';

  public notification: any;

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
    this.isShowedEntityDetails$ = this._store.select(fromWizard.isShowedEntityDetails).distinctUntilChanged();
    this.isLoading$ = this._store.select(fromWizard.isLoading);

    this._store.select(fromWizard.areUndoRedoEnabled)
      .takeUntil(this._componentDestroyed)
      .subscribe((actions: any) => {
        this.undoEnabled = actions.undo;
        this.redoEnabled = actions.redo;
        this._cd.markForCheck();
      });

    this._store.select(fromWizard.getValidationErrors)
      .takeUntil(this._componentDestroyed)
      .subscribe((validations: any) => {
        this.validations = validations;
        this._cd.markForCheck();
      });

    this._store.select(fromWizard.isPristine).distinctUntilChanged()
      .takeUntil(this._componentDestroyed)
      .subscribe((isPristine: boolean) => {
        this.isPristine = isPristine;
        this._cd.markForCheck();
      });

    this._store.select(fromWizard.getWorkflowType)
      .takeUntil(this._componentDestroyed)
      .subscribe((type) => this.workflowType = type);

    this._store.select(fromWizard.getDebugResult)
      .takeUntil(this._componentDestroyed)
      .subscribe(debugResult => {
        this.genericError = debugResult && debugResult.genericError ? debugResult.genericError : null;
        this._cd.markForCheck();
      });

    let handler;
    this._store.select(fromWizard.getWizardNofications)
      .takeUntil(this._componentDestroyed)
      .subscribe((notification) => {
        if ((notification.message && notification.message.length) || notification.templateType) {
          this.notification = {
            ...this.notification,
            visible: false
          };
          this._cd.markForCheck();
          clearTimeout(handler);
          setTimeout(() => {
            this.notification = {
              ...notification,
              visible: true
            };
            this._cd.markForCheck();
            handler = setTimeout(() => {
              this.notification = {
                ...this.notification,
                visible: false
              };
              this._cd.markForCheck();
            }, notification.time === 0 ? 10000000 : (notification.time || 4000));
          });
        } else {
          this.notification = {};
        }
      });

    this.menuOptions$ = this._store.select(fromWizard.getMenuOptions);
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
