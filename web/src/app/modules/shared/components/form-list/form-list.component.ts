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
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    forwardRef,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChange,
    SimpleChanges,
    ViewChildren,
    ViewChild
} from '@angular/core';
import {
    ControlValueAccessor, FormControl, NG_VALIDATORS, NG_VALUE_ACCESSOR, FormBuilder, FormArray, FormGroup, NgForm, Validator, Validators
} from '@angular/forms';
import { Subscription } from 'rxjs/Subscription';
import { ErrorMessagesService } from "app/services";

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
export class FormListComponent implements Validator, ControlValueAccessor, OnInit, OnDestroy {

    @Input() public formListData: any;
    @Input() public label = '';
    @Input() public qaTag = '';
    @Input() required = false;
    @Input() forceValidations = false;

    @ViewChild('inputForm') public inputForm: NgForm;

    public item: any = {};
    public form: FormGroup;
    public items: FormArray;
    public hasErrors = false;

    onChange = (_: any) => { };
    onTouched = () => { };

    constructor(private formBuilder: FormBuilder, private _cd: ChangeDetectorRef, public errorsService: ErrorMessagesService) { };

    ngOnInit() {
        for (const field of this.formListData.fields) {
            this.item[field.propertyId] = ['', Validators.required];
        }

        this.items = this.formBuilder.array([]);
        this.form = new FormGroup({
            items: this.items
        });
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
    }

    getItemClass(field: any): string {
        if (field.width) {
            return 'col-xs-' + field.width;
        }
        const type: string = field.propertyType;
        if (type === 'boolean') {
            return 'check-column';
        }
        const length = this.formListData.fields.length;
        if (length === 1) {
            return 'col-xs-6';
        } else if (length < 4) {
            return 'col-xs-' + 12 / length;
        } else {
            return 'col-xs-4';
        }
    }

    writeValue(data: any): void {
        if (data && Array.isArray(data) && data.length) {
            this.items.controls = [];
            for (const value of data) {
                const item: any = {};
                for (const field of this.formListData.fields) {
                    item[field.propertyId] = [value[field.propertyId]];
                }
                const form: FormGroup = this.formBuilder.group(item);
                this.items.push(form);
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

    setDisabledState(isDisabled: boolean) {
        if (isDisabled) {
            this.form.disable();
        } else {
            this.form.enable();
        }
    }

    changeValue(value: any): void {
        this.onChange(this.items.value);
    }

    validate(c: FormGroup): { [key: string]: any; } {
        if (this.required) {
            if (!this.items.controls.length) {
                return  {
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

        return error ? {
            formListError: {
                valid: false
            }
        } : null;

    }

    ngOnDestroy(): void {

    }
}
