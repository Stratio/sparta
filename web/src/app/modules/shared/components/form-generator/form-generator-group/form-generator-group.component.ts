/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { OnDestroy } from '@angular/core/core';
import {
   Component, OnInit, Output, EventEmitter, Input, forwardRef, ChangeDetectorRef,
   ChangeDetectionStrategy, ViewChild
} from '@angular/core';
import {
   ControlValueAccessor, FormGroup,
   NG_VALUE_ACCESSOR, Validator, NG_VALIDATORS, NgForm
} from '@angular/forms';
import { Subscription } from 'rxjs/Rx';
import { StHorizontalTab } from '@stratio/egeo';
import { TranslateService } from '@ngx-translate/core';

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
   @ViewChild('groupForm') public groupForm: NgForm;

   public options: StHorizontalTab[] = [];
   public activeOption = '';
   public formGroup: FormGroup;
   private stFormGroupSubcription: Subscription;


   constructor(private _cd: ChangeDetectorRef, private translate: TranslateService) { }

   public changeFormOption($event: StHorizontalTab) {
      this.activeOption = $event.id;
   }

   ngOnInit(): void {
      this._cd.detach();
      this.options = this.formData.map((category: any) => ({
         id: category.name,
         text: ''
      }));

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

   registerOnTouched(fn: any): void { }

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

