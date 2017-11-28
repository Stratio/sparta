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
    ChangeDetectorRef,
    Component,
    forwardRef,
    Input,
    OnDestroy,
    OnInit,
    ViewChild,
    OnChanges,
    SimpleChanges
} from '@angular/core';
import {
    ControlValueAccessor, NG_VALIDATORS, NG_VALUE_ACCESSOR, FormBuilder, FormArray, FormGroup, NgForm, Validator, Validators
} from '@angular/forms';
import { Subscription } from 'rxjs/Subscription';
import { ErrorMessagesService } from 'app/services';

@Component({
    selector: 'form-list',
    templateUrl: './form-list.template.html',
    styleUrls: ['./form-list.styles.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FormListComponent),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => FormListComponent),
            multi: true
        }],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormListComponent implements Validator, ControlValueAccessor, OnChanges, OnInit, OnDestroy {

    @Input() public formListData: any;
    @Input() public label = '';
    @Input() public qaTag = '';
    @Input() required = false;
    @Input() errors: any = {};
    @Input() tooltip = '';
    @Input() forceValidations = false;

    @ViewChild('inputForm') public inputForm: NgForm;

    public item: any = {};
    public internalControl: FormGroup;
    public items: FormArray;
    public isError = false;
    public isDisabled = false;

    public visibleConditions: any = [];
    public visibleConditionsOR: any = [];

    private internalControlSubscription: Subscription;

    private itemssubscription: Array<Subscription> = [];

    ngOnChanges(changes: SimpleChanges): void {
        if (this.forceValidations && this.internalControl) {
            this.writeValue(this.internalControl.value);
        }
        this._cd.markForCheck();
    }

    onChange = (_: any) => { };
    onTouched = () => { };

    constructor(private formBuilder: FormBuilder, private _cd: ChangeDetectorRef, public errorsService: ErrorMessagesService) { };

    ngOnInit() {
        for (const field of this.formListData.fields) {
            this.item[field.propertyId] = this.addItemValidation(field);

            if (field.visible && field.visible.length) {
                this.visibleConditions.push({
                    propertyId: field.propertyId,
                    conditions: field.visible[0]
                });
            }

            if (field.visibleOR && field.visibleOR.length) {
                this.visibleConditionsOR.push({
                    propertyId: field.propertyId,
                    conditions: field.visibleOR[0]
                });
            }
        }

        this.items = this.formBuilder.array([]);
        this.internalControl = new FormGroup({
            items: this.items
        });

        this.internalControlSubscription = this.internalControl.valueChanges.subscribe((form: FormGroup) => {
            this.onChange(this.items.value);
        });
    }

    addItemValidation(field: any) {
        const item: any = {};
        if (field.propertyType === 'boolean' || !field.required) {
            return [''];
        } else {
            return ['', Validators.required];
        }
    }

    //create empty item
    createItem(): FormGroup {
        return this.formBuilder.group(this.item);
    }

    deleteItem(i: number) {
        this.items.removeAt(i);
        this.onChange(this.items.value);
    }

    addItem(): void {
        this.items.push(this.createItem());
        const i = this.items.length - 1;
        this.addObservableVisibleRule(i);
    }

    addObservableVisibleRule(index: number) {
        const subscriptionsHandler: any = [];
        const itemGroup: any = this.items.controls[index];

        this.visibleConditions.forEach((propertyConditions: any) => {   // each property
            propertyConditions.conditions.forEach((prop: any) => {      // each property conditions
                const subscription: Subscription = itemGroup.controls[prop.propertyId].valueChanges.subscribe((value: any) => {
                    this.checkDisabledField(propertyConditions.conditions, itemGroup, propertyConditions.propertyId, 'AND');
                });
                this.checkDisabledField(propertyConditions.conditions, itemGroup, propertyConditions.propertyId, 'AND');
                subscriptionsHandler.push(subscription);
            });
        });

        this.visibleConditionsOR.forEach((propertyConditions: any) => {   // each property
            propertyConditions.conditions.forEach((prop: any) => {      // each property conditions
                const subscription: Subscription = itemGroup.controls[prop.propertyId].valueChanges.subscribe((value: any) => {
                    this.checkDisabledField(propertyConditions.conditions, itemGroup, propertyConditions.propertyId, 'OR');
                });
                this.checkDisabledField(propertyConditions.conditions, itemGroup, propertyConditions.propertyId, 'OR');
                subscriptionsHandler.push(subscription);
            });
        });

        this.itemssubscription.push(subscriptionsHandler);
    }

    checkDisabledField(propertyConditions: any, group: any, propertyId: string, conditionType: string) {
        let enable = (conditionType !== 'OR');
        propertyConditions.forEach((rule: any) => {
            if (conditionType === 'OR' && rule.value == group.controls[rule.propertyId].value) {
                enable = true;
            }
            if (conditionType !== 'OR' && rule.value != group.controls[rule.propertyId].value && group.controls[rule.propertyId].enabled) {
                enable = false;
            }
        });
        if (enable) {
            group.controls[propertyId].enable();
        } else {
            group.controls[propertyId].disable();
        }
    }


    showError(): boolean {
        return this.isError && (!this.internalControl.pristine || this.forceValidations) && !this.isDisabled;
    }

    getItemClass(field: any): string {
        if (field.width) {
            return 'list-item col-xs-' + field.width;
        }
        const type: string = field.propertyType;
        if (type === 'boolean') {
            return 'list-item check-column';
        }
        const length = this.formListData.fields.length;
        if (length === 1) {
            return 'list-item col-xs-6';
        } else if (length < 4) {
            return 'list-item col-xs-' + 12 / length;
        } else {
            return 'list-item col-xs-4';
        }
    }

    writeValue(data: any): void {
        if (data && Array.isArray(data) && data.length) {
            this.items.controls = [];
            for (const value of data) {
                const item: any = {};
                for (const field of this.formListData.fields) {
                    item[field.propertyId] = this.addItemValidation(field);
                    item[field.propertyId][0] = value[field.propertyId];
                }
                const form: FormGroup = this.formBuilder.group(item);
                this.items.push(form);
                const i = this.items.length - 1;
                this.addObservableVisibleRule(i);
            }
            this._cd.detectChanges();
        } else {
            this.items.controls = [];
        }
    }


    // Registry the change function to propagate internal model changes
    registerOnChange(fn: (_: any) => void): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState(disable: boolean): void {
        this.isDisabled = disable;
        if (this.isDisabled && this.internalControl && this.internalControl.enabled) {
            this.internalControl.disable();
        } else if (!this.isDisabled && this.internalControl && this.internalControl.disabled) {
            this.internalControl.enable();
        }
        this._cd.markForCheck();
    }

    validate(c: FormGroup): { [key: string]: any; } {
        if (this.required) {
            if (!this.items.controls.length) {
                this.isError = true;
                return {
                    formListError: {
                        valid: false
                    }
                };
            }
        }

        let error = false;
        this.items.controls.forEach((control: any) => {
            if (control.invalid) {
                error = true;
            }
        });
        this.isError = error;
        return error ? {
            formListError: {
                valid: false
            }
        } : null;

    }

    ngOnDestroy(): void {
        this.internalControlSubscription && this.internalControlSubscription.unsubscribe();
        if (this.itemssubscription.length) {
            this.itemssubscription.forEach((rowSubscriptions: any) => {
                rowSubscriptions.forEach((subscription: any) => {
                    subscription.unsubscribe();
                });
            });
        }
    }
}
