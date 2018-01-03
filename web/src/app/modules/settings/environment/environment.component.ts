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

import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    ViewChild,
    ViewContainerRef
} from '@angular/core';
import { Store } from '@ngrx/store';
import { ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs/Rx';
import { NgForm, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';

import * as fromRoot from 'reducers';
import * as environmentActions from 'actions/environment';

import { ImportEnvironmentModalComponent } from './import-environment-modal/import-environment-modal.component';
import { StModalService, StModalMainTextSize, StModalType, StModalWidth } from '@stratio/egeo';
import { TranslateService } from '@ngx-translate/core';
import { BreadcrumbMenuService } from 'services';
import { ErrorMessagesService } from 'app/services/error-messages.service';

@Component({
    selector: 'environment',
    templateUrl: './environment.template.html',
    styleUrls: ['./environment.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EnvironmentComponent implements OnInit, OnDestroy {

    @ViewChild('environmentModal', { read: ViewContainerRef }) target: any;
    @ViewChild('environmentsForm') public environmentsForm: NgForm;

    public breadcrumbOptions: any = [];


    public item: any = {};
    public internalControl: FormGroup;
    public items: FormArray;
    public filter = '';
    public model: any = [];
    public forceValidations = false;
    public environmentList: Subscription;
    public filtered: any = [];
    public filteredValue = '';
    public menuOptions: any = [{
        name: 'Export environment data',
        value: 'export'
    },
    {
        name: 'Import environment data',
        value: 'import'
    }];
    public fields = [
    {
        'propertyId': 'name',
        'propertyName': '_KEY_',
        'propertyType': 'text',
        'width': 3
    },
    {
        'propertyId': 'value',
        'propertyName': '_VALUE_',
        'propertyType': 'text',
        'width': 6
    }];

    ngOnInit() {

        this.items = this.formBuilder.array([]);
        this.internalControl = new FormGroup({
            variables: this.items
        });

        for (const field of this.fields) {
            this.item[field.propertyId] = ['', Validators.required];
        }

        this._modalService.container = this.target;
        this.store.dispatch(new environmentActions.ListEnvironmentAction());
        this.environmentList = this.store.select(fromRoot.getEnvironmentList).subscribe((envList: any) => {
            this.model = envList.variables;
            this.initForm(this.model);
            this._cd.detectChanges();
        });
        /*this.store.dispatch(new errorsActions.SavedDataNotificationAction());
        console.log(this.store.select(fromRoot.pendingSavedData).take(1).);*/
    }

    initForm(variables: Array<any>) {
         if (variables && Array.isArray(variables) && variables.length) {
            this.items.controls = [];
            for (const value of variables) {
                const item: any = {};
                for (const field of this.fields) {
                    item[field.propertyId] = [Validators.required];
                    item[field.propertyId][0] = value[field.propertyId];
                }
                const form: FormGroup = this.formBuilder.group(item);
                this.items.push(form);
            }
            this._cd.detectChanges();
        } else {
            this.items.controls = [];
        }
    }

    isHidden(value: any){
        return !(value.name.indexOf(this.filter) > -1);
    }

    addItem(): void {
        this.internalControl.markAsDirty();
        this.items.push(this.formBuilder.group(this.item));
        this.filter = '';
    }

    deleteItem(i: number) {
        this.internalControl.markAsDirty();
        this.items.removeAt(i);
    }


    updateFilter() {
        this.onSearchResult(this.filteredValue);
    }

    saveEnvironment() {
        if (this.internalControl.valid) {
            this.forceValidations = false;
            this.store.dispatch(new environmentActions.SaveEnvironmentAction(this.internalControl.value));
        } else {
            this.forceValidations = true;
        }
    }

    selectedMenuOption($event: any) {
        if ($event.value === 'import') {
            this.importEnvironmentData();
        } else {
            this.store.dispatch(new environmentActions.ExportEnvironmentAction());
        }
    }

    public importEnvironmentData(): void {
        this._modalService.show({
            qaTag: 'new-workflow-json-modal',
            modalTitle: 'Import environment data',
            outputs: {
                onCloseImportModal: this.onCloseImportModal.bind(this)
            },
            modalWidth: StModalWidth.COMPACT,
            mainText: StModalMainTextSize.BIG,
            modalType: StModalType.NEUTRAL
        }, ImportEnvironmentModalComponent);
    }

    onSearchResult($event: any) {
        this.filter = $event;
    }

    public onCloseImportModal() {
        this._modalService.close();
    }

    constructor(private store: Store<fromRoot.State>, private _cd: ChangeDetectorRef,
        private _modalService: StModalService, private translate: TranslateService, private formBuilder: FormBuilder,
        public breadcrumbMenuService: BreadcrumbMenuService, public errorsService: ErrorMessagesService) {
       this.breadcrumbOptions = breadcrumbMenuService.getOptions();
    }

    ngOnDestroy(): void {
        this.environmentList && this.environmentList.unsubscribe();
    }

}
