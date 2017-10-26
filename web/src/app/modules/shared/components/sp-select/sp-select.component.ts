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
    EventEmitter,
    forwardRef,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    SimpleChange,
    SimpleChanges,
    ViewChildren,
    ElementRef
} from '@angular/core';
import {
    ControlValueAccessor, FormControl, NG_VALIDATORS, NG_VALUE_ACCESSOR, Validators,
    ValidatorFn
} from '@angular/forms';
import { Subscription } from 'rxjs/Subscription';

@Component({
    selector: 'sp-select',
    templateUrl: './sp-select.template.html',
    styleUrls: ['./sp-select.styles.scss'],
    host: {
        '(document:click)': 'onClick($event)'
    },
    providers: [
        { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => SpSelectComponent), multi: true },
        { provide: NG_VALIDATORS, useExisting: forwardRef(() => SpSelectComponent), multi: true }
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpSelectComponent implements ControlValueAccessor, OnChanges, OnInit, OnDestroy {
    @Input() placeholder = '';
    @Input() name = '';
    @Input() label = '';
    @Input() fieldType: 'text' | 'number' | 'password' = 'text';
    @Input() public selectedValue: any = {};
    @Input() errors: any;
    @Input() options: any = [];
    @Input() qaTag: string;
    @Input() forceValidations = false;
    @Input() tooltip: string;
    @Input() maxLength: number;
    @Input() min: number;
    @Input() max: number;
    @Input() isFocused: boolean = false;
    @Input() readonly: boolean = false;
    @Input() public errorRequiredMessage: string = '';

    @Input()
    get value(): any {
        return this._value;
    }

    set value(value: any) {
        this._value = value;
    }

    public active = false;

    @Output() change: EventEmitter<any> = new EventEmitter<any>();

    @ViewChildren('input') vc: any;

    public disabled: boolean = false; // To check disable
    public focus: boolean = false;
    public internalControl: FormControl;
    public errorMessage: string = undefined;
    public selectedOption: any;

    private sub: Subscription;
    private _value: any;
    private valueChangeSub: Subscription;

    constructor(private _cd: ChangeDetectorRef, private _eref: ElementRef) { }

    onChange = (_: any) => { };
    onTouched = () => { };

    onClick(event: any): void {
        if (!this._eref.nativeElement.contains(event.target)) {// or some similar check
            this.active = false;
        }
    }


    validate(control: FormControl): any {
      if (this.sub) {
         this.sub.unsubscribe();
      }
      this.sub = control.statusChanges.subscribe(() => this.checkErrors(control));
      this.checkErrors(control);
    }

    ngOnChanges(change: any): void {
        if (this.forceValidations && this.internalControl) {
            this.writeValue(this.internalControl.value.value);
        }
        this._cd.markForCheck();
    }

    showOptions() {
        if(this.internalControl.disabled) {
            this.active = false;
        } else {
            this.active = !this.active;
        }
    }

    selectOption(option: any) {
        this.internalControl.setValue(option);
        this.onChange(option.value);
        this.active = false;
        this.change.emit(option.value);
    }

    ngOnInit(): void {
        this.internalControl = new FormControl({});
        this.valueChangeSub = this.internalControl.valueChanges.subscribe((value) => this.onChange(value.value));
    }

    ngAfterViewInit(): void {
        if (this.isFocused) {
            this.focus = true;
            this.vc.first.nativeElement.focus();
        }
    }

    ngOnDestroy(): void {
        if (this.valueChangeSub) {
            this.valueChangeSub.unsubscribe();
        }
        if (this.sub) {
            this.sub.unsubscribe();
        }
    }

    // When value is received from outside
    writeValue(newValue: any): void {
        const val = this.options.find((option: any) => option.value === newValue);
        this.internalControl.setValue(val ? val : {
            label: newValue,
            value: newValue
        });
    }

    // Registry the change function to propagate internal model changes
    registerOnChange(fn: (_: any) => void): void {
        this.onChange = fn;
    }

    // Registry the touch function to propagate internal touch events TODO: make this function.
    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState(disable: boolean): void {
        console.log(this.options, disable);
        this.disabled = disable;
        if (this.disabled && this.internalControl.enabled) {
            this.internalControl.disable();
        } else if (!this.disabled && this.internalControl && this.internalControl.disabled) {
            this.internalControl.enable();
        }
        this._cd.markForCheck();
    }

    // When status change call this function to check if have errors
    private checkErrors(control: FormControl): void {
        let errors: { [key: string]: any } = control.errors;
        this.errorMessage = this.getErrorMessage(errors);
        this._cd.markForCheck();
    }

    showError(): boolean {
        return this.errorMessage !== undefined && (!this.internalControl.pristine || this.forceValidations) && !this.focus && !this.disabled;
    }

    // Get error message in function of error list.
    private getErrorMessage(errors: { [key: string]: any }): string {
        if (!errors) {
            return undefined;
        }

        if (!this.errors) {
            return '';
        }

        if (errors.hasOwnProperty('required')) {
            return this.errors.required || this.errors.generic || '';
        }
        if (errors.hasOwnProperty('fieldType')) {
            return this.errors.type || this.errors.generic || '';
        }
        if (errors.hasOwnProperty('minlength')) {
            return this.errors.minLength || this.errors.generic || '';
        }
        if (errors.hasOwnProperty('maxlength')) {
            return this.errors.maxLength || this.errors.generic || '';
        }
        if (errors.hasOwnProperty('pattern')) {
            return this.errors.pattern || this.errors.generic || '';
        }
        if (errors.hasOwnProperty('min')) {
            return this.errors.min || this.errors.generic || '';
        }
        if (errors.hasOwnProperty('max')) {
            return this.errors.max || this.errors.generic || '';
        }
        return '';
    }
}
