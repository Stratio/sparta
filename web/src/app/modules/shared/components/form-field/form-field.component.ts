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

import { OnDestroy } from '@angular/core/core';
import { Component, OnInit, Output, EventEmitter, Input, ChangeDetectorRef, ViewChildren, forwardRef } from '@angular/core';
import { ControlValueAccessor, FormGroup, FormControl, NG_VALUE_ACCESSOR, Validator, NG_VALIDATORS } from '@angular/forms';
import { Subscription } from 'rxjs/Subscription';
import { Subject } from 'rxjs/Rx';
import { StInputError } from '@stratio/egeo';
import { ErrorMessagesService } from 'services';

@Component({
    selector: 'form-field',
    templateUrl: './form-field.template.html',
    styleUrls: ['./form-field.styles.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FormFieldComponent),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => FormFieldComponent),
            multi: true
        }]
})
export class FormFieldComponent implements Validator, ControlValueAccessor, OnInit, OnDestroy {

    @Input() field: any;
    @Input() stFormGroup: FormGroup;
    @Input() forceValidations = false;

    public stFormControl: FormControl;
    public stFormControlSubcription: Subscription;
    public isDisabled = false; // To check disable
    public isVisible = true;
    public disableSubscription: Subscription[] = [];
    public stModel: any = false;
    public errors: StInputError = {};

    private registeredOnChange: (_: any) => void;

    ngOnInit(): void {
        this.stFormControl = new FormControl();

        setTimeout(() => {
            if (this.field.visible && this.field.visible.length) {
                for (const field of this.field.visible[0]) {
                    this.stFormGroup.controls[field.propertyId].valueChanges.subscribe((value) => {
                        this.disableField();
                    });
                }
            }
            setTimeout(() => {
                this.stFormControl.updateValueAndValidity();
            });
        });
    }

    disableField(): void {
        let enable = false;
        this.field.visible[0].forEach((rule: any) => {
            if(rule.value == this.stFormGroup.controls[rule.propertyId].value){
                enable = true;
            }
        });
        if (enable) {
            this.stFormGroup.controls[this.field.propertyId].enable();
        } else {
            this.stFormGroup.controls[this.field.propertyId].disable();
        }
    }

    getEmptyValue(): any {

        switch (this.field.propertyType) {
            case 'text':
                return '';
            case 'select':
                return '';
            case 'boolean':
                return false;
            case 'list':
                return [];
            default:
                return '';

        }
    }

    onChange(value: any) {
        value = value && value !== undefined && value !== '' ? value : this.getEmptyValue();
        this.stFormControl.setValue(value);
        this.stModel = value;
    }

    writeValue(value: any): void {
        this.onChange(value);
    }

    registerOnChange(fn: any): void {
        this.stFormControlSubcription = this.stFormControl.valueChanges.subscribe(fn);
    }

    registerOnTouched(fn: any): void {

    }

    setDisabledState(isDisabled: boolean) {
        if (isDisabled) {
            this.stFormControl.disable();
        } else {
            this.stFormControl.enable();
        }
    }

    validate(c: FormGroup): { [key: string]: any; } {
        return (this.stFormControl.valid) ? null : {
            formFieldError: {
                valid: false
            }
        };
    }

    constructor(private _cd: ChangeDetectorRef, public errorsService: ErrorMessagesService) { }

    ngOnDestroy(): void {
        this.stFormControlSubcription.unsubscribe();
    }


}

