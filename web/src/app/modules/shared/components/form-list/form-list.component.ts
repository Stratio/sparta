/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
   ChangeDetectionStrategy,
   ChangeDetectorRef,
   Component,
   forwardRef,
   Input,
   OnDestroy,
   OnInit,
   ViewChild,
} from '@angular/core';
import {
   ControlValueAccessor, NG_VALIDATORS, NG_VALUE_ACCESSOR, FormBuilder, FormArray, FormGroup, NgForm, Validator, Validators
} from '@angular/forms';
import { Subscription } from 'rxjs/Subscription';

import { ErrorMessagesService } from 'app/services';

@Component({
   selector: 'form-list',
   templateUrl: './form-list.template.html',
   styleUrls: ['./form-list.styles.scss'],
   providers: [
      {
         provide: NG_VALUE_ACCESSOR,
         useExisting: forwardRef(() => FormListComponent),
         multi: true
      },
      {
         provide: NG_VALIDATORS,
         useExisting: forwardRef(() => FormListComponent),
         multi: true
      }],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormListComponent implements Validator, ControlValueAccessor, OnInit, OnDestroy {

   @Input() public formListData: any;
   @Input() public label = '';
   @Input() public complexForm = false;
   @Input() public qaTag = '';
   @Input() required = false;
   @Input() errors: any = {};
   @Input() tooltip = '';
   @Input() forceValidations = false;
   @Input() valueDictionary: any;
   @Input() variableList: Array<any> = [];
   @Input() showVars: boolean;

   @ViewChild('inputForm') public inputForm: NgForm;

   public item: any = {};
   public internalControl: FormGroup;
   public items: FormArray;
   public isError = false;
   public isDisabled = false;

   public visibleConditions: any = [];
   public visibleConditionsOR: any = [];

   private internalControlSubscription: Subscription;
   private itemssubscription: Array<Array<Subscription>> = [];

   onChange = (_: any) => { };
   onTouched = () => { };

   ngOnInit() {
     this.initForm();
     this.formListData.forEach(field => {
         field.classed = this._getItemClass(field);
         this.item[field.propertyId] = this.addItemValidation(field);
         this.getDisabledConditions(field);
      });
   }

   initForm() {
      this.items = this.formBuilder.array([]);
      this.internalControl = new FormGroup({
         items: this.items
      });
      this.internalControlSubscription = this.internalControl.valueChanges.subscribe((form: FormGroup) => {
         this.onChange(this.items.value);
      });
   }

   getDisabledConditions(field: any) {
      if (field.visible && field.visible.length) {
         this.visibleConditions.push({
            propertyId: field.propertyId,
            conditions: field.visible[0]
         });
      }
      if (field.visibleOR && field.visibleOR.length) {
         this.visibleConditionsOR.push({
            propertyId: field.propertyId,
            conditions: field.visibleOR[0]
         });
      }
   }

   addItemValidation(field: any) {
      const value = field.default ? field.default : '';
      if (field.propertyType === 'boolean' || !field.required) {
         return [value];
      } else {
         return [value, Validators.required];
      }
   }

   //create empty item
   createItem(): FormGroup {
      return this.formBuilder.group(this.item);
   }

   deleteItem(i: number) {
      this.items.removeAt(i);
      this.onChange(this.items.value);
   }

   addItem(): void {
      this.items.push(this.createItem());
      const i = this.items.length - 1;
      this.addObservableVisibleRules(i);
   }

   addObservableVisibleRules(index: number) {
      const itemGroup: any = this.items.controls[index];
      this.itemssubscription.push([
         ...this._addRules(this.visibleConditions, itemGroup, 'AND'),
         ...this._addRules(this.visibleConditionsOR, itemGroup, 'OR')
      ]);
   }

   private _addRules(conditions: Array<any>, itemGroup: any, conditionType) {
      const subscriptions = [];
      conditions.forEach((propertyConditions: any) => {   // each property
         propertyConditions.conditions.forEach((prop: any) => {      // each property conditions
            const subscription: Subscription = itemGroup.controls[prop.propertyId].valueChanges.subscribe((value: any) => {
               this.checkDisabledField(propertyConditions.conditions, itemGroup, propertyConditions.propertyId, conditionType === 'OR');
            });
            this.checkDisabledField(propertyConditions.conditions, itemGroup, propertyConditions.propertyId, conditionType === 'OR');
            subscriptions.push(subscription);
         });
      });
      return subscriptions;
   }

   checkDisabledField(propertyConditions: any, group: any, propertyId: string, orCondition: boolean) {
      let enable = (!orCondition);
      propertyConditions.forEach((rule: any) => {
         if (orCondition && rule.value === group.controls[rule.propertyId].value) {
            enable = true;
         }
         if (!orCondition && rule.value !== group.controls[rule.propertyId].value && group.controls[rule.propertyId].enabled) {
            enable = false;
         }
      });
      enable && !this.isDisabled ? group.controls[propertyId].enable() : group.controls[propertyId].disable();
   }

   getVariableList(field: any) {
      if (this.valueDictionary && (this.formListData.showSchemaFields || field.showSchemaFields) && this.valueDictionary.formFieldsVariables) {
        return this.valueDictionary.formFieldsVariables;
      }
      if (this.valueDictionary && (this.formListData.showInputSteps || field.showInputSteps) && this.valueDictionary.inputStepsVariables) {
        return this.valueDictionary.inputStepsVariables;
      }
      return this.variableList;
   }

   showError(): boolean {
      return this.isError && (!this.internalControl.pristine || this.forceValidations) && !this.isDisabled;
   }

   private _getItemClass(field: any): string {
      if (field.width) {
         return 'list-item col-sm-' + field.width;
      }
      const type: string = field.propertyType;
      if (type === 'boolean') {
         return 'list-item check-column';
      }
      const length = this.formListData.length;
      if (length === 1) {
         return 'list-item col-sm-6';
      } else if (length < 4) {
         return 'list-item col-sm-' + 12 / length;
      } else {
         return 'list-item col-sm-4';
      }
   }

   writeValue(data: any): void {
      if (data && Array.isArray(data) && data.length) {
         this.items.controls = [];
         data.forEach(value => {
            const item: any = {};
            this.formListData.forEach(field => {
               item[field.propertyId] = this.addItemValidation(field);
               item[field.propertyId][0] = { value: value[field.propertyId], disabled: this.isDisabled };
            });
            const form: FormGroup = this.formBuilder.group(item);
            /** fix initial validation in egeo inputs: the input initial value is not validated until it changes*/
            setTimeout(() => form.patchValue(value));
            this.items.push(form);
            const i = this.items.length - 1;
            this.addObservableVisibleRules(i);
         });

         this._cd.markForCheck();
      } else {
         this.items.controls = [];
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
               formListError: {
                  valid: false
               }
            };
         }
      }
      let error = false;
      this.items.controls.forEach((control: any) => {
         if (control.invalid) {
            error = true;
         }
      });
      this.isError = error;
      return error ? {
         formListError: {
            valid: false
         }
      } : null;
   }

   constructor(private formBuilder: FormBuilder, private _cd: ChangeDetectorRef, public errorsService: ErrorMessagesService) { };

   ngOnDestroy(): void {
      this.internalControlSubscription && this.internalControlSubscription.unsubscribe();
      if (this.itemssubscription.length) {
         this.itemssubscription.forEach((rowSubscriptions: any) => {
            rowSubscriptions.forEach((subscription: any) => {
               subscription.unsubscribe();
            });
         });
      }
   }
}
