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

    @Input() isNodeSelected = false;

    @ViewChild('wizardModal', { read: ViewContainerRef }) target: any;
    
    public isShowedEntityDetails$: Observable<boolean>;
    public menuOptions$: Observable<Array<FloatingMenuModel>>;
    public workflowName: string = '';
    public nameSubscription: Subscription;

    private inputListSubscription: Subscription;
    private outputListSubscription: Subscription;
    private inputList: Array<any> = [];
    private outputList: Array<any> = [];



    constructor(private route: Router, private currentActivatedRoute: ActivatedRoute, private store: Store<fromRoot.State>,
        private _cd: ChangeDetectorRef, private _modalService: StModalService) { }

    ngOnInit(): void {
        this._modalService.container = this.target;
        // this.store.dispatch(new inputActions.ListInputAction());
        // this.store.dispatch(new outputActions.ListOutputAction());
        this.isShowedEntityDetails$ = this.store.select(fromRoot.isShowedEntityDetails);
        this.nameSubscription = this.store.select(fromRoot.getWorkflowName).subscribe((name: string) => {
            this.workflowName = name;
        });

        this.menuOptions$ = this.store.select(fromRoot.getMenuOptions);
        /*this.inputListSubscription = this.store.select(fromRoot.getInputList).subscribe((data: any) => {
            this.inputList = data;
            this.menuOptions[0].subMenus[0].subMenus = this.getTemplateMenuNames(data);
            this._cd.detectChanges();
        });

        this.outputListSubscription = this.store.select(fromRoot.getOutputList).subscribe((data: any) => {
            this.outputList = data;
            this.menuOptions[2].subMenus[0].subMenus = this.getTemplateMenuNames(data);
            this._cd.detectChanges();
        });*/

    }

    selectedMenuOption($event: any): void {
        this.store.dispatch(new wizardActions.SelectedCreationEntityAction($event));
    }

    deleteWorkflow(): void {
        this.store.dispatch(new wizardActions.DeleteEntityAction());
    }

    onBlurWorkflowName(): void {
        this.store.dispatch(new wizardActions.ChangeWorkflowNameAction(this.workflowName));
    }

    getTemplateMenuNames(templateList: Array<any>) {
        return templateList.map((template) => {
            return {
                name: template.name,
                value: {
                    type: 'template',
                    name: template.name
                }
            };
        });
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

    onCloseConfirmationModal(event: any) {
        this._modalService.close();
        if(event === '1') {
            this.onSaveWorkflow.emit();
        } else {
            this.route.navigate(['']);
        }
    }

    ngOnDestroy(): void {
        this.nameSubscription && this.nameSubscription.unsubscribe();
    }


}
