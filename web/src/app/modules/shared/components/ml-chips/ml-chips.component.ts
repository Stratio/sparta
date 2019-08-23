/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  forwardRef,
  HostBinding,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALIDATORS, NG_VALUE_ACCESSOR, Validator } from '@angular/forms';
import { StDropDownMenuGroup, StDropDownMenuItem } from '../st-dropdown-menu/st-dropdown-menu.interface';

@Component({
  selector: 'ml-chips',
  templateUrl: './ml-chips.template.html',
  styleUrls: ['./ml-chips.styles.scss'],
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => MlChipsComponent), multi: true },
    { provide: NG_VALIDATORS, useExisting: forwardRef(() => MlChipsComponent), multi: true }],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MlChipsComponent implements ControlValueAccessor, Validator, OnInit, OnDestroy, OnChanges {
  @Input() label = '';
  @Input() required = false;

  public hasErrors = false;
  public chipsRequired = false;

  public chipsContent: Array<string> = [];

  constructor(private _cd: ChangeDetectorRef) {}


  /**
   * Implements
   */
  onChange = (_: any) => {};

  /**
   * Implements
   */
  onTouched = () => {};

  ngOnInit(): void {
    this.chipsRequired = this.required;
    console.log('CHIPS: OnInit');
  }

  ngOnChanges(changes: SimpleChanges): void {
    // this.writeValue(this.chipsContent);
    console.log('CHIPS: OnChanges');
  }

  /**
   * Implements
   */
  writeValue(value: any): void {
    if (Array.isArray(value)) {
      this.chipsContent = value.slice();
      this._cd.markForCheck();
    }
    // this.onChange(this.chipsContent);
  }

  /**
   * Implements
   */
  validate(control: FormControl): any {
    this.checkErrors(control);
  }

  /**
   * Implements
   */
  registerOnChange(fn: (_: any) => void): void {
    this.onChange = fn;
  }

  /**
   * Implements
   */
  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  private checkErrors(control: FormControl): void {
    this.hasErrors = !!(control.errors && Object.keys(control.errors).length);
  }

  ngOnDestroy(): void {
    console.log('CHIPS: OnDestroy');
  }
}
