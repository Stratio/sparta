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
    ViewChild,
    ElementRef,
    HostListener
} from '@angular/core';
import {
    ControlValueAccessor, FormControl, NG_VALIDATORS, NG_VALUE_ACCESSOR, FormBuilder, FormArray, FormGroup, NgForm, Validator
} from '@angular/forms';
import { Subscription } from 'rxjs/Subscription';

@Component({
    selector: 'form-string-list',
    templateUrl: './form-string-list.template.html',
    styleUrls: ['./form-string-list.styles.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FormListStringComponent),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => FormListStringComponent),
            multi: true
        }],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormListStringComponent implements Validator, ControlValueAccessor, OnInit, OnDestroy {

    @Input() public formListData: any;
    @Input() public label = '';
    @Input() public qaTag = '';
    @Input() errors: any = {};
    @Input() required = false;
    @Input() forceValidations = false;
    @Input() contextualHelp = '';
    @Input() tooltip = '';

    @ViewChild('inputForm') public inputForm: NgForm;
    @ViewChild('newElement') newElementInput: ElementRef;


    public item: any = {};
    public internalControl: FormGroup;
    public items: FormArray;
    public focus: boolean = false;
    public isDisabled = false;
    public isError = false;
    public innerInputContent = '';


    public fieldName = '';

    onChange = (_: any) => { };
    onTouched = () => { };

    addElementFocus() {
        this.focus = true;
        this.newElementInput.nativeElement.focus();
    }

    onkeyPress($event: any) {
        if ($event.keyCode === 13 && this.innerInputContent.length) {
            this.items.push(this.createItem(this.innerInputContent));
            this.innerInputContent = '';
        }
    }

    constructor(private formBuilder: FormBuilder, private _cd: ChangeDetectorRef) { };

    ngOnInit() {
        this.fieldName = this.formListData.fields[0].propertyId;
        this.items = this.formBuilder.array([]);
        this.internalControl = new FormGroup({
            items: this.items
        });

        this.internalControl.valueChanges.subscribe((value) => {
            this.onChange(this.items.value);
        });
    }

    //create empty item
    createItem(value: any): FormGroup {
        const item: any = {};
        item[this.fieldName] = value;
        const form: FormGroup = this.formBuilder.group(item);
        return this.formBuilder.group(item);
    }

    deleteItem(i: number) {
        this.items.removeAt(i);
    }

    addItem(): void {

    }

    showError(): boolean {
        return this.isError && (!this.internalControl.pristine || this.forceValidations) && !this.focus && !this.isDisabled;
    }

    /** Style functions */
    onFocus(event: Event): void {
        this.focus = true;
    }

    onFocusOut(event: Event): void {
        this.focus = false;
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
            this.addItem();
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
                    formStringListError: {
                        valid: false
                    }
                };
            } else {
                this.isError = false;
            }
        }
    }

    ngOnDestroy(): void {

    }
}
