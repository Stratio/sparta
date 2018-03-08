/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, OnDestroy, ChangeDetectorRef, Output, EventEmitter, Input, ViewChild, ViewContainerRef } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs/Rx';
import { Location } from '@angular/common';
import { StModalService } from '@stratio/egeo';

import * as fromRoot from 'reducers';
import * as wizardActions from 'actions/wizard';
import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';
import { WizardModalComponent } from './../wizard-modal/wizard-modal.component';


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
    @Input() selectedSegment: any;
    @Input() workflowName = '';
    @Input() workflowVersion = 0;

    @ViewChild('titleFocus') titleElement: any;
    @ViewChild('nameForm') public nameForm: NgForm;
    @ViewChild('wizardModal', { read: ViewContainerRef }) target: any;

    public workflowNamePattern = '^[a-z0-9-]*$';
    public isShowedEntityDetails$: Observable<boolean>;
    public menuOptions$: Observable<Array<FloatingMenuModel>>;
    public isLoading$: Observable<boolean>;
    public showErrors = false;
    public workflowType = '';

    public editName = false;
    public undoEnabled = false;
    public redoEnabled = false;
    public isPristine = true;
    public validations: any = {};

    public menuSaveOptions: any = [
        {
            name: 'Input',
            icon: 'icon-login',
            value: 'action',
        },
        {
            name: 'Input',
            icon: 'icon-login',
            value: 'action',
        },

    ];
    private _validationSubscription: Subscription;
    private _areUndoRedoEnabledSubscription: Subscription;
    private _isPristineSubscription: Subscription;
    private _workflowTypeSubscription: Subscription;


    constructor(private route: Router, private currentActivatedRoute: ActivatedRoute, private store: Store<fromRoot.State>,
        private _cd: ChangeDetectorRef, private _modalService: StModalService, private _location: Location) { }

    ngOnInit(): void {
        this._modalService.container = this.target;
        this.isShowedEntityDetails$ = this.store.select(fromRoot.isShowedEntityDetails).distinctUntilChanged();
        this.isLoading$ = this.store.select(fromRoot.isLoading);

        this._areUndoRedoEnabledSubscription = this.store.select(fromRoot.areUndoRedoEnabled).subscribe((actions: any) => {
            this.undoEnabled = actions.undo;
            this.redoEnabled = actions.redo;
        });

        this._validationSubscription = this.store.select(fromRoot.getValidationErrors).subscribe((validations: any) => {
            this.validations = validations;
            this._cd.detectChanges();
        });

        this._isPristineSubscription = this.store.select(fromRoot.isPristine).distinctUntilChanged().subscribe((isPristine: boolean) => {
            this.isPristine = isPristine;
            this._cd.detectChanges();
        });
        this._workflowTypeSubscription = this.store.select(fromRoot.getWorkflowType).subscribe((type) => this.workflowType = type);
        this.menuOptions$ = this.store.select(fromRoot.getMenuOptions);
    }

    selectedMenuOption($event: any): void {
        this.store.dispatch(new wizardActions.SelectedCreationEntityAction($event));
    }

    showSettings() {
        this.store.dispatch(new wizardActions.ShowSettingsAction());
    }

    onBlurWorkflowName(): void {
        this.editName = false;
        this.store.dispatch(new wizardActions.ChangeWorkflowNameAction(this.workflowName));
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
        this._areUndoRedoEnabledSubscription && this._areUndoRedoEnabledSubscription.unsubscribe();
        this._validationSubscription && this._validationSubscription.unsubscribe();
        this._isPristineSubscription && this._isPristineSubscription.unsubscribe();
    }
}
