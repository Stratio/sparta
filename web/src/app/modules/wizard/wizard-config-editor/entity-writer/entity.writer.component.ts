import { Subscription } from 'rxjs/Rx';
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


import * as writerTemplate from 'data-templates/writer.json';
import { Component, OnInit, Output, EventEmitter, ChangeDetectionStrategy, Input, forwardRef, OnDestroy, ViewChild } from '@angular/core';
import { NG_VALUE_ACCESSOR, NG_VALIDATORS, Validator, ControlValueAccessor, FormGroup, NgForm } from '@angular/forms';

@Component({
    selector: 'entity-writer',
    templateUrl: './entity-writer.template.html',
    styleUrls: ['./entity-writer.styles.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => EntityWriterComponent),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => EntityWriterComponent),
            multi: true
        }],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EntityWriterComponent implements Validator, ControlValueAccessor, OnInit, OnDestroy {

    @Input() forceValidations = false;
    @Output() onCloseConfirmModal = new EventEmitter<string>();
    @ViewChild('writerForm') public groupForm: NgForm;

    public writerSettings: any = [];
    public writerModel: any = {};

    private stFormGroupSubcription: Subscription;

    constructor() {
        this.writerSettings = writerTemplate;
    }

    ngOnInit() {

    }

    writeValue(value: any): void {
        if (value) {
            this.writerModel = value;
        } else {
            this.writerModel = {};
        }
    }

    registerOnChange(fn: any): void {
        this.stFormGroupSubcription = this.groupForm.valueChanges.subscribe(() => {
            fn(this.writerModel);
        });
    }

    registerOnTouched(fn: any): void {

    }

    setDisabledState(isDisabled: boolean): void {
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
