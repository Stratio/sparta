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

import { Subject } from 'rxjs/Subject';
import { OnDestroy } from '@angular/core/core';
import { Component, OnInit, Output, EventEmitter, Input, forwardRef, ChangeDetectorRef } from '@angular/core';
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

    @Input() public stModel: any = {};
    @Output() public stModelChange: EventEmitter<any> = new EventEmitter<any>();

    public formDataAux: any;
    public stFormGroupSubcription: Subscription;
    public formDataValues: any = [];
    notifyChangeSubject: Subject<any> = new Subject();
    private registeredOnChange: (_: any) => void;


    ngOnInit(): void { }

    writeValue(value: any): void {
        if (value) {
            this.stFormGroup.patchValue(value);
        } else {
            this.stModel = {};
        }
        this.notifyChangeSubject.next(value);
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

    registerOnTouched(fn: any): void {

    }

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
}

