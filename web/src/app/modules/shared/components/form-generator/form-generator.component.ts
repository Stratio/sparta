/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Subject } from 'rxjs/Subject';
import { Component, OnInit, Output, EventEmitter, Input, forwardRef, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { ControlValueAccessor, FormGroup, FormControl, NG_VALUE_ACCESSOR, Validator, NG_VALIDATORS } from '@angular/forms';
import { Subscription } from 'rxjs/Subscription';

@Component({
    selector: 'form-generator',
    templateUrl: './form-generator.template.html',
    styleUrls: ['./form-generator.styles.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FormGeneratorComponent),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => FormGeneratorComponent),
            multi: true
        }]
})
export class FormGeneratorComponent implements Validator, ControlValueAccessor, OnInit, OnDestroy {

    @Input() public formData: any; // data template
    @Input() public stFormGroup: FormGroup;
    @Input() forceValidations = false;
    @Input() public subFormNumber = 0;
    @Input() arity: any;
    @Input() disabledForm = false;
    @Input() public stModel: any = {};

    @Output() public stModelChange: EventEmitter<any> = new EventEmitter<any>();

    public formDataAux: any;
    public stFormGroupSubcription: Subscription;
    public formDataValues: any = [];

    ngOnInit(): void { }

    writeValue(value: any): void {
        if (value) {
            this.stFormGroup.patchValue(value);
        } else {
            this.stModel = {};
        }
    }

    ngOnChanges(change: any): void {
        if (change.formData) {
            // remove all controls before repaint form
            this.stFormGroup.controls = {};    // reset controls
            this.formDataValues = [];
            const properties = change.formData.currentValue;
            for (const prop of properties) {
                const formControl = new FormControl();
                this.stFormGroup.addControl(prop.propertyId ? prop.propertyId : prop.name, formControl);
                this.formDataValues.push({
                    formControl: formControl,
                    field: prop
                });
            }
        }
    }

    registerOnChange(fn: any): void {
        this.stFormGroupSubcription = this.stFormGroup.valueChanges.subscribe(fn);
    }

    registerOnTouched(fn: any): void { }

    validate(c: FormGroup): { [key: string]: any; } {
        return (this.stFormGroup.valid) ? null : {
            formGeneratorError: {
                valid: false
            }
        };
    }

    getClass(width: string): string {
        return width ? 'col-xs-' + width : 'col-xs-6';
    }

    constructor(private _cd: ChangeDetectorRef) {
        if (!this.stFormGroup) {
            this.stFormGroup = new FormGroup({});
        }
    }

    ngOnDestroy(): void {
        if (this.stFormGroupSubcription) {
            this.stFormGroupSubcription.unsubscribe();
        }
    }

    setDisabledState(isDisabled: boolean) {
        if (isDisabled) {
            this.stFormGroup.disable();
        } else {
            this.stFormGroup.enable();
        }
    }
}

