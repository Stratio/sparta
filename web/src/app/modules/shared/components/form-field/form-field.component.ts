/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { OnDestroy } from '@angular/core/core';
import { Component, OnInit, Input, ChangeDetectorRef, forwardRef } from '@angular/core';
import { ControlValueAccessor, FormGroup, FormControl, NG_VALUE_ACCESSOR, Validator, NG_VALIDATORS } from '@angular/forms';
import { Subscription } from 'rxjs/Subscription';
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
    @Input() disabled = false;

    public stFormControl: FormControl;
    public stFormControlSubcription: Subscription;
    public isDisabled = false; // To check disable
    public isVisible = true;
    public disableSubscription: Subscription[] = [];
    public stModel: any = false;
    public errors: StInputError = {};

    ngOnInit(): void {
        this.stFormControl = new FormControl();
        if (!this.disabled) {
            setTimeout(() => {
                if (this.field.visible && this.field.visible.length) {
                    for (const field of this.field.visible[0]) {
                        this.disableSubscription.push(this.stFormGroup.controls[field.propertyId].valueChanges.subscribe((value) => {
                            this.disableField();
                        }));
                    }
                }

                if (this.field.visibleOR && this.field.visibleOR.length) {
                    for (const field of this.field.visibleOR[0]) {
                        this.disableSubscription.push(this.stFormGroup.controls[field.propertyId].valueChanges.subscribe((value) => {
                            this.disableFieldOR();
                        }));
                    }
                }

                if (this.field.visibleOR && this.field.visibleOR.length) {
                    for (const field of this.field.visibleOR[0]) {
                        this.disableSubscription.push(this.stFormGroup.controls[field.propertyId].valueChanges.subscribe((value) => {
                            this.disableFieldOR();
                        }));
                    }
                }
                setTimeout(() => {
                    this.stFormControl.updateValueAndValidity();
                });
            });
        }

    }

    // TODO: refactor in only one method
    disableField(): void {
        let enable = true;
        this.field.visible[0].forEach((rule: any) => {
            if (rule.value != this.stFormGroup.controls[rule.propertyId].value) {
                enable = false;
            }
        });
        if (enable && !this.isDisabled) {
            this.stFormGroup.controls[this.field.propertyId].enable();
        } else {
            this.stFormGroup.controls[this.field.propertyId].disable();
        }
    }

    disableFieldOR(): void {
        let enable = false;
        this.field.visibleOR[0].forEach((rule: any) => {
            if (rule.value == this.stFormGroup.controls[rule.propertyId].value && this.stFormGroup.controls[rule.propertyId].enabled) {
                enable = true;
            }
        });
        if (enable && !this.isDisabled) {
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
                return this.field.default ? this.field.default : '';
            case 'boolean':
                return false;
            case 'switch':
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
        this.isDisabled = isDisabled;
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
        this.disableSubscription.map((subcription: Subscription) => {
            subcription.unsubscribe();
        });
    }


}

