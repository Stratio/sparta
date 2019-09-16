/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, forwardRef, OnInit, ChangeDetectionStrategy, Input, OnDestroy } from '@angular/core';
import { Store, select } from '@ngrx/store';
import { ControlValueAccessor, Validator, FormGroup, FormControl, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { StDropDownMenuItem } from '@stratio/egeo';

import * as crossdatInputActions from './actions/crossdata';
import * as fromCrossdataInput from './reducers/index';
import { SpCrossdataInputValue } from './sp-crossdata-input.model';

@Component({
  selector: 'sp-crossdata-input',
  styleUrls: ['sp-crossdata-input.component.scss'],
  templateUrl: 'sp-crossdata-input.component.html',
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => SpCrossdataInputComponent), multi: true }
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpCrossdataInputComponent implements ControlValueAccessor, OnDestroy, OnInit, Validator {

  @Input() label: string;
  @Input() qaTag: string;
  @Input() forceValidations: boolean;
  @Input() required: boolean;
  @Input() errorRequiredMessage: string;

  public formGroup: FormGroup;
  public databaseFormControl = new FormControl('', Validators.required);
  public tableFormControl = new FormControl('', Validators.required);

  public databasesOptions$: Observable<StDropDownMenuItem[]>;
  public tablesOptions$: Observable<StDropDownMenuItem[]>;
  public isDisabled: boolean;

  private _databaseSubscription: Subscription;
  private _tablesSubscription: Subscription;
  private _valueSubscription: Subscription;

  private _onChange: (_: any) => void;

  constructor(private _store: Store<fromCrossdataInput.State>) { }

  public ngOnInit(): void {
    this.formGroup = new FormGroup({
      database: this.databaseFormControl,
      table: this.tableFormControl
    });
    this._store.dispatch(new crossdatInputActions.GetDatabases());
    this.databasesOptions$ = this._store.pipe(select(fromCrossdataInput.getDatabasesOptions));
    this._databaseSubscription = this.databaseFormControl.valueChanges.subscribe((value) => {
      if (value && value.length) {
        this._store.dispatch(new crossdatInputActions.GetDatabaseTables(value));
        this.tableFormControl.enable();
      } else {
        this.tableFormControl.disable();
      }
      this.tablesOptions$ = this._store.pipe(select(fromCrossdataInput.getDatabaseTables(this.databaseFormControl.value)));
    });

    this._valueSubscription = this.formGroup.valueChanges.subscribe((value: SpCrossdataInputValue) => {
      if (this._onChange) {
        this._onChange(value);
      }
    });

    if (this.required) {
      this.databaseFormControl.markAsDirty();
      this.tableFormControl.markAsDirty();
    }
  }

  public ngOnDestroy(): void {
    if (this._databaseSubscription) {
      this._databaseSubscription.unsubscribe();
    }
    if (this._tablesSubscription) {
      this._tablesSubscription.unsubscribe();
    }
    if(this._valueSubscription) {
      this._valueSubscription.unsubscribe();
    }
  }

  public changeDatabase() {
    this.tableFormControl.setValue('');
  }

  public validate() {
    return this.required && this.formGroup.invalid ? { 'custom': true } : null;
  }

  public writeValue(value: SpCrossdataInputValue): void {
    if (value && value.database) {
      if(!value.table) {
        value.table = null;
      }
      this.formGroup.setValue(value);
    } else {
      this.formGroup.setValue({
        database: null,
        table: null
      });
    }
  }

  public registerOnChange(fn: (_: any) => void): void {
    this._onChange = fn;
  }

  public registerOnTouched(fn: any): void {
  }

  public setDisabledState(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
    if (isDisabled) {
      this.formGroup.disable();
    } else {
      this.formGroup.enable();
    }
  }
}
