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

import { Component, OnInit, OnDestroy, ChangeDetectorRef, Output, EventEmitter, Input } from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { Observable, Subscription } from 'rxjs/Rx';
import { Router, ActivatedRoute } from '@angular/router';
import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';


// actions
import * as inputActions from 'actions/input';
import * as outputActions from 'actions/output';
import * as wizardActions from 'actions/wizard';


@Component({
    selector: 'wizard-header',
    styleUrls: ['wizard-header.styles.scss'],
    templateUrl: 'wizard-header.template.html'
})

export class WizardHeaderComponent implements OnInit, OnDestroy {

    @Output() onZoomIn = new EventEmitter();
    @Output() onZoomOut = new EventEmitter();
    @Output() onCenter = new EventEmitter();

    @Input() isNodeSelected = false;

    public isShowedEntityDetails$: Observable<boolean>;
    public menuOptions$: Observable<Array<FloatingMenuModel>>;

    private inputListSubscription: Subscription;
    private outputListSubscription: Subscription;
    private inputList: Array<any> = [];
    private outputList: Array<any> = [];

    constructor(private route: Router, private currentActivatedRoute: ActivatedRoute, private store: Store<fromRoot.State>,
    private _cd: ChangeDetectorRef) { }

    ngOnInit(): void {
       // this.store.dispatch(new inputActions.ListInputAction());
       // this.store.dispatch(new outputActions.ListOutputAction());
       this.isShowedEntityDetails$ = this.store.select(fromRoot.isShowedEntityDetails);
       
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

    selectedMenuOption($event: any) {
        this.store.dispatch(new wizardActions.SelectedCreationEntityAction($event));
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

    cancelWizard(): void {
        this.route.navigate(['..'], { relativeTo: this.currentActivatedRoute });

    }

    ngOnDestroy(): void {

    }
}
