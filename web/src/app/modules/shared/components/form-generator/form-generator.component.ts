/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, Output, EventEmitter, Input, forwardRef, ChangeDetectorRef, OnDestroy, OnChanges } from '@angular/core';
import { ControlValueAccessor, FormGroup, FormControl, NG_VALUE_ACCESSOR, Validator, NG_VALIDATORS } from '@angular/forms';
import { Subscription, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

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
export class FormGeneratorComponent implements Validator, ControlValueAccessor, OnDestroy, OnChanges {

  @Input() formData: any; // data template
  @Input() stFormGroup: FormGroup;
  @Input() forceValidations = false;
  @Input() subFormNumber = 0;
  @Input() arity: any;
  @Input() disabledForm = false;
  @Input() stModel: any = {};
  @Input() valueDictionary: any = {};
  @Input() variableList: Array<any> = [];
  @Input() showVars: boolean;
  @Input() customValidators: any = {};

  @Output() public stModelChange: EventEmitter<any> = new EventEmitter<any>();

  public formDataAux: any;
  public stFormGroupSubcription: Subscription;
  public formDataValues: any = [];
  public categories = [];

  private _componentDestroyed = new Subject();

  writeValue(value: any): void {
    if (value) {
      let val;
      try {
        val = typeof value === 'string' ? JSON.parse(value) : value;
      } catch (error) {
        val = value;
      }
      this.stFormGroup.patchValue(val);
    } else {
      this.stModel = {};
    }
  }

  registerOnChange(fn: any): void {
    this.stFormGroupSubcription = this.stFormGroup.valueChanges.subscribe(fn);
  }

  registerOnTouched(fn: any): void { }

  validate(c: FormGroup): { [key: string]: any; } {
    const controls = this.stFormGroup.controls;
    return (this.stFormGroup.valid) ? null : {
      formGeneratorError: {
        valid: false,
        errors: Object.keys(controls).reduce((acc, key) => {
          if (controls[key].invalid) {
            acc.push({
              name: key,
              errors: controls[key].errors
            });
          }
          return acc;
        }, [])
      }
    };
  }

  constructor(private _cd: ChangeDetectorRef) {
    if (!this.stFormGroup) {
      this.stFormGroup = new FormGroup({});
    }
  }

  ngOnChanges(change: any): void {
    if (change.formData) {
      // remove all controls before repaint form
      this.stFormGroup.controls = {};    // reset controls
      this.formDataValues = [];
      const properties = change.formData.currentValue;
      for (const prop of properties) {
        prop.classed = this._getClass(prop.width);
        const formControl = new FormControl();
        this.stFormGroup.addControl(prop.propertyId ? prop.propertyId : prop.name, formControl);
        this.formDataValues.push({
          formControl: formControl,
          field: prop
        });

        formControl.statusChanges.pipe(takeUntil(this._componentDestroyed)).subscribe(status => {
          if (status === 'DISABLED' && formControl.value === null) {
            formControl.setValue(prop.default);
          }
        });
      }
    }
  }

  ngOnDestroy(): void {
    if (this.stFormGroupSubcription) {
      this.stFormGroupSubcription.unsubscribe();
    }
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
  }

  setDisabledState(isDisabled: boolean) {
    if (isDisabled) {
      this.stFormGroup.disable();
    } else {
      this.stFormGroup.enable();
    }
  }

  toggleCategory(i: number) {
    if (this.categories.indexOf(i) > -1) {
      this.categories = this.categories.filter(cat => cat !== i);
    } else {
      this.categories = [...this.categories, i];
    }
  }

  private _getClass(width: string): string {
    return width ? 'col-xs-' + width : 'col-xs-8';
  }
}
