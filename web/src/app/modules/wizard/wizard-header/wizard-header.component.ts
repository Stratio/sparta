///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Component, OnInit, OnDestroy, ChangeDetectorRef, Output, EventEmitter, Input, ViewChild, ViewContainerRef } from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { Observable, Subscription } from 'rxjs/Rx';
import { Router, ActivatedRoute } from '@angular/router';
import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';


// actions
import * as inputActions from 'actions/input';
import * as outputActions from 'actions/output';
import * as wizardActions from 'actions/wizard';
import { StModalService, StModalWidth, StModalMainTextSize, StModalType } from '@stratio/egeo';
import { WizardModalComponent } from '@app/wizard/wizard-modal/wizard-modal.component';
import { NgForm } from "@angular/forms";


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
    @Output() onShowSettings = new EventEmitter();
    @Output() onSaveWorkflow = new EventEmitter();
    @Output() onEditEntity = new EventEmitter();
    @Output() deleteSelection = new EventEmitter();

    @Input() isNodeSelected = false;
    @Input() selectedSegment: any;

    @ViewChild('titleFocus') titleElement: any;
    @ViewChild('nameForm') public nameForm: NgForm;
    @ViewChild('wizardModal', { read: ViewContainerRef }) target: any;

    public workflowNamePattern = '^[a-z0-9-]*$';
    public isShowedEntityDetails$: Observable<boolean>;
    public menuOptions$: Observable<Array<FloatingMenuModel>>;
    public workflowName: string = '';
    public nameSubscription: Subscription;
    

    private inputListSubscription: Subscription;
    private outputListSubscription: Subscription;
    private areUndoRedoEnabledSubscription: Subscription;
    private inputList: Array<any> = [];
    private outputList: Array<any> = [];
    public editName = false;

    public undoEnabled = false;
    public redoEnabled = false;

    constructor(private route: Router, private currentActivatedRoute: ActivatedRoute, private store: Store<fromRoot.State>,
        private _cd: ChangeDetectorRef, private _modalService: StModalService) { }

    ngOnInit(): void {
        this._modalService.container = this.target;
        this.store.dispatch(new inputActions.ListInputAction());
        this.store.dispatch(new outputActions.ListOutputAction());
        this.isShowedEntityDetails$ = this.store.select(fromRoot.isShowedEntityDetails);
        this.nameSubscription = this.store.select(fromRoot.getWorkflowName).subscribe((name: string) => {
            this.workflowName = name;
        });

        this.areUndoRedoEnabledSubscription = this.store.select(fromRoot.areUndoRedoEnabled).subscribe((actions: any) => {
            this.undoEnabled = actions.undo;
            this.redoEnabled = actions.redo;
        });

        this.menuOptions$ = this.store.select(fromRoot.getMenuOptions);

    }

    selectedMenuOption($event: any): void {
        this.store.dispatch(new wizardActions.SelectedCreationEntityAction($event));
    }

    editWorkflowName(): void {
        this.editName = true;
        this._cd.markForCheck();
        setTimeout(()=> {
            this.titleElement.nativeElement.focus();
        });
    }

    eventHandler() {
        this.editName = true;
        this._cd.markForCheck();
        setTimeout(()=> {
            this.titleElement.nativeElement.focus();
        });
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
        const sub: any = this._modalService.show({
            qaTag: 'exit-workflow',
            modalTitle: 'Exit workflow',
            outputs: {
                onCloseConfirmModal: this.onCloseConfirmationModal.bind(this)
            },
            modalWidth: StModalWidth.COMPACT,
            mainText: StModalMainTextSize.BIG,
            modalType: StModalType.NEUTRAL
        }, WizardModalComponent);
    }

    public saveWorkflow(): void {
        if (this.nameForm.valid) {
            this.onSaveWorkflow.emit();
        }
    }

    onCloseConfirmationModal(event: any) {
        this._modalService.close();
        if (event === '1') {
            if(this.nameForm.valid) {
                this.onSaveWorkflow.emit();
            }
        } else {
            this.route.navigate(['']);
        }
    }

    undoAction() {
        this.store.dispatch(new wizardActions.UndoChangesAction());
    }

    redoAction() {
        this.store.dispatch(new wizardActions.RedoChangesAction());
    }

    ngOnDestroy(): void {
        this.nameSubscription && this.nameSubscription.unsubscribe();
        this.areUndoRedoEnabledSubscription && this.areUndoRedoEnabledSubscription.unsubscribe();
    }


}
