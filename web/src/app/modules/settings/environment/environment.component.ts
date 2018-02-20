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

import * as fromRoot from './reducers';
import * as environmentActions from './actions/environment';
import { generateJsonFile } from 'utils';

import { ImportEnvironmentModalComponent } from './components/import-environment-modal/import-environment-modal.component';
import { StModalService } from '@stratio/egeo';
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

    public duplicated: Array<string> = [];

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

            setTimeout(() => {
                if (this.internalControl.invalid) {
                    this.forceValidations = true;
                    this.internalControl.markAsPristine();
                    this._cd.detectChanges();
                }
            });
        });
    }

    initForm(variables: Array<any>) {
        if (variables && Array.isArray(variables) && variables.length) {
            this.items.controls = [];
            for (const value of variables) {
                const item: any = {};
                for (const field of this.fields) {
                    item[field.propertyId] = [value[field.propertyId], Validators.required];
                }
                const form: FormGroup = this.formBuilder.group(item);
                this.items.push(form);
            }
        } else {
            this.items.controls = [];
        }
        this.internalControl.markAsPristine();
        this.getDuplicated();
        this._cd.detectChanges();
    }

    isHidden(value: any) {
        return !(value.name.toLowerCase().indexOf(this.filter) > -1);
    }

    downloadVariables() {
        generateJsonFile(new Date().getTime().toString(), this.internalControl.value.variables);
    }

    uploadVariables(event: any) {
        const reader = new FileReader();
        reader.readAsText(event[0]);
        reader.onload = (loadEvent: any) => {
            try {
                this.initForm([...this.internalControl.value.variables].concat(JSON.parse(loadEvent.target.result)));
            } catch (error) {

            }
        };
    }

    getDuplicated() {
        this.duplicated = this.internalControl.value.variables.map((variable: any) => variable.name)
            .filter((variable: string, index: number, variables: Array<string>) => variable.length && variables.indexOf(variable) !== index);
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
        this.getDuplicated();
        if (!this.duplicated.length && this.internalControl.valid) {
            this.forceValidations = false;
            this.store.dispatch(new environmentActions.SaveEnvironmentAction(this.internalControl.value));
            this.internalControl.markAsPristine();
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
            modalTitle: 'Import environment data',
            outputs: {
                onCloseImportModal: this.onCloseImportModal.bind(this)
            },
        }, ImportEnvironmentModalComponent);
    }

    onSearchResult($event: any) {
        this.filter = $event.text.toLowerCase();
    }

    public onCloseImportModal() {
        this.forceValidations = true;
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
