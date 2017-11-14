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

import { Subject } from 'rxjs/Rx';
import { OnDestroy } from '@angular/core/core';
import {
    Component, OnInit, Output, EventEmitter, Input, forwardRef, ChangeDetectorRef,
    ChangeDetectionStrategy, ViewChild
} from '@angular/core';
import {
    ControlValueAccessor, FormGroup, FormControl, FormArray,
    NG_VALUE_ACCESSOR, Validator, NG_VALIDATORS, NgForm
} from '@angular/forms';
import { Subscription } from 'rxjs/Rx';
import { StHorizontalTab } from '@stratio/egeo';
import { TranslateService } from "@ngx-translate/core";

@Component({
    selector: 'form-generator-group',
    templateUrl: './form-generator-group.template.html',
    styleUrls: ['./form-generator-group.styles.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FormGeneratorGroupComponent),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => FormGeneratorGroupComponent),
            multi: true
        }],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormGeneratorGroupComponent implements Validator, ControlValueAccessor, OnInit, OnDestroy {

    @Input() public formData: Array<any>; // data template
    @Input() public forceValidations = false;
    @Input() public qaTag: string;

    @Input() public stModel: any = {};
    @Output() public stModelChange: EventEmitter<any> = new EventEmitter<any>();

    private stFormGroupSubcription: Subscription;

    @ViewChild('groupForm') public groupForm: NgForm;

    public options: StHorizontalTab[] = [];
    public activeOption = '';
    public formGroup: FormGroup;

    constructor(private _cd: ChangeDetectorRef, private translate: TranslateService) {
    }

    public changeFormOption($event: StHorizontalTab) {
        this.activeOption = $event.id;
    }


    ngOnInit(): void {
        this._cd.detach();
        this.options = this.formData.map((category: any) => {
            return {
                id: category.name,
                text: ''
            };
        });

        const translateKey = 'FORM_TABS.';

        this.translate.get(this.options.map((option: any) => {
            return translateKey + option.id.toUpperCase();
        })).subscribe((value: { [key: string]: string }) => {
            this.options.map((option: any) => {
                const key = value[translateKey + option.id.toUpperCase()];
                option.text = key ? key : option.id;
                return option;
            });
            this._cd.reattach();
        });
    }

    writeValue(value: any): void {
        if (value) {
            this.stModel = value;
        } else {
            this.stModel = {};
        }
    }

    registerOnChange(fn: any): void {
        this.stFormGroupSubcription = this.groupForm.valueChanges.subscribe(fn);
    }

    registerOnTouched(fn: any): void {

    }

    validate(c: FormGroup): { [key: string]: any; } {
        return (this.groupForm.valid) ? null : {
            formGeneratorGroupError: {
                valid: false
            }
        };
    }

    ngOnDestroy(): void {
        this.stFormGroupSubcription && this.stFormGroupSubcription.unsubscribe();
    }
}

