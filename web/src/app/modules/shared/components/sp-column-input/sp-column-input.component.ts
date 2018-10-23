/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
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
   ViewChildren,
   ElementRef,
   AfterViewInit
} from '@angular/core';
import {
   ControlValueAccessor, FormControl, NG_VALIDATORS, NG_VALUE_ACCESSOR
} from '@angular/forms';
import { Subscription } from 'rxjs/Subscription';
import { uniq as _uniq } from 'lodash';
import { SpColumnInputError, SpColumnInputVariable } from './sp-column-input.models';

const navigates = ['ArrowDown', 'ArrowUp', 'Enter'];

@Component({
   selector: 'sp-column-input',
   templateUrl: './sp-column-input.component.html',
   styleUrls: ['./sp-column-input.component.scss'],
   providers: [
      { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => SpColumnInputComponent), multi: true },
      { provide: NG_VALIDATORS, useExisting: forwardRef(() => SpColumnInputComponent), multi: true }
   ],
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class SpColumnInputComponent implements ControlValueAccessor, OnChanges, OnInit, OnDestroy, AfterViewInit {

   @Input() placeholder = '';
   @Input() name = '';
   @Input() label = '';
   @Input() fieldType: 'text' | 'number' | 'password' = 'text';
   @Input() errors: SpColumnInputError;
   @Input() qaTag: string;
   @Input() forceValidations = false;
   @Input() contextualHelp: string;
   @Input() maxLength: number;
   @Input() min: number;
   @Input() max: number;
   @Input() isFocused = false;
   @Input() readonly = false;
   @Input() variableList: Array<SpColumnInputVariable> = [];
   @Input() showVars = false;

   @Input()
   get value(): any {
      return this._value;
   }

   set value(value: any) {
      this._value = value;
   }

   @Output() change: EventEmitter<any> = new EventEmitter<any>();

   @ViewChildren('input') vc: any;


   public disabled = false; // To check disable
   public focus = false;
   public internalControl: FormControl;
   public errorMessage: string = undefined;
   public selectVarMode = false;
   public filteredVariableList: Array<any>;
   public pristine = true;
   public selectedIndex = 0;
   public defaultList = true;
   public filteredVariableGroupList: Array<any>;
   public emptyVariables = true;
   public typeSelected = 'field';



   public selectedFilter = undefined;

   private scrollList: any;
   private sub: Subscription;
   private _value: any;
   private valueChangeSub: Subscription;
   private internalInputModel: any = '';
   public options =  [];
   public selectedOption = 'default';
   public isVarValue = false;

   private _element: any;

   private focusPristine = true;

   constructor(private _cd: ChangeDetectorRef, private _elementRef: ElementRef) {
      this._element = _elementRef.nativeElement;
   }

   onChange = (_: any) => { };
   onTouched = () => { };


   validate(control: FormControl): any {
      if (this.sub) {
         this.sub.unsubscribe();
      } else {
         this.checkErrors(control);
      }
      this.sub = control.statusChanges.subscribe(() => this.checkErrors(control));
   }

   ngOnChanges(change: any): void {
      if (this.forceValidations && this.internalControl) {
         this.writeValue(this.internalControl.value);
      }
      this._cd.markForCheck();
   }

   ngOnInit(): void {

      this.internalControl = new FormControl(this.internalInputModel);
      this.valueChangeSub = this.internalControl.valueChanges.subscribe((value) => {
         this.focusPristine = false;
         if (this.selectVarMode) {
            this.filterVarListValue(value);
            this.defaultList = !!value.length;
         }
      });

      this.addOptions(this.variableList ? _uniq(this.variableList.map(variable => variable.valueType)).map(item => ({ label: item, value: item })) : []);
      this.groupFilteredVariableList();
      this.filteredVariableList = this.variableList;
      this.defaultList = !!this.internalControl.value.length;
      if (!this.variableList) {
          this.showVars = false;
      }
   }

   private addOptions(options) {
      if (options.length) {
         this.selectedOption = options[0].value;
         this.options = options;
      }
   }

   toggleVarType(event, filter) {
      event.preventDefault();
      event.stopPropagation();
      this.selectedFilter = filter === this.selectedFilter ? undefined : filter;
   }

   filterVarListValue(value: string): void {
      const compval = value.toUpperCase();
      this.filteredVariableList = this.variableList ? this.variableList.filter((variable: SpColumnInputVariable) =>
            variable.name.toUpperCase().indexOf(compval) > -1 || (variable.valueType && variable.valueType.toUpperCase().indexOf(compval) > -1)) : [];
      this.emptyVariables = !this.filteredVariableList.length;
      this.defaultList = !this.emptyVariables || !!value.length;

   }

   ngAfterViewInit(): void {
      if (this.isFocused) {
         this.focus = true;
         this.vc.first.nativeElement.focus();
      }
      if (this.forceValidations) {
         this.onChange(this.internalControl.value);
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

   showVarCatalog(event) {
      event.preventDefault();  //prevent default DOM action
      event.stopPropagation();   //stop bubbling
      this.vc.first.nativeElement.focus();
      if (!this.selectVarMode) {
         this.selectVarMode = true;
         this.filterVarListValue(this.internalControl.value);
      }


   }

   groupFilteredVariableList() {
      this.filteredVariableGroupList = this.options.map(option =>
         ({
            filter: option.label,
            list: this.variableList.filter(variable => variable.valueType === option.label)
         }));

   }

   valuechange(event) {
      this.pristine = false;
      this.onChange(this.isVarValue && ['env', 'var'].includes(this.typeSelected)  ? `{{{${this.internalControl.value}}}}` : this.internalControl.value);
   }


   selectVar(event: any, variable: SpColumnInputVariable) {
      event.preventDefault();  //prevent default DOM action
      event.stopPropagation();   //stop bubbling
      if (this.options.map(option => option.label).includes(variable.valueType)) {
         this.isVarValue = true;
         this.internalControl.setValue(variable.name);
         this.onChange(variable.valueType === 'env' ? `{{{${this.internalControl.value}}}}` : this.internalControl.value);
      } else {
         this.isVarValue = false;
         this.internalControl.setValue(variable.name);
         this.onChange(this.internalControl.value);
      }

      this.focusPristine = true;
      this.vc.first.nativeElement.blur();
      this.selectedIndex = 0;
      this.typeSelected = variable.valueType;
      this.selectVarMode = false;
   }




   resetVarValue() {
      if (this.isVarValue && !this.focusPristine) {
         this.isVarValue = false;
         this.onChange(this.internalControl.value);
      }
      this.focusPristine = true;
      this.selectVarMode = false;
      this.filterVarListValue(this.internalControl.value);
   }

   // When value is received from outside
   writeValue(value: any): void {
      if (typeof value === 'object') {
         value = JSON.stringify(value);
      }
      this.isVarValue = false;
      if (this.showVars && this.selectedOption === 'env' && value) {
         if (value.length > 6 && value.indexOf('{{{') === 0 && value.indexOf('}}}') === value.length - 3) {
            value = value.replace('{{{', '').replace('}}}', '');
            this.isVarValue = true;

         } else if (value.length > 4 && value.indexOf('{{') === 0 && value.indexOf('}}') === value.length - 2) {
            value = value.replace('{{', '').replace('}}', '');
            this.isVarValue = true;
         }
      }
      if (this.forceValidations) {
         this.onChange(this.isVarValue && ['env', 'var'].includes(this.typeSelected) ? `{{{${value}}}}` : value);
      }
      this.internalInputModel = value;
      this._value = value;
      this.internalControl.setValue(value);
      this.focusPristine = true;
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
      this.disabled = disable;
      if (this.disabled && this.internalControl && this.internalControl.enabled) {
         this.internalControl.disable();
      } else if (!this.disabled && this.internalControl && this.internalControl.disabled) {
         this.internalControl.enable();
      }
      this._cd.markForCheck();
   }

   showError(): boolean {
      return this.errorMessage !== undefined &&
         (!this.pristine || this.forceValidations) && !this.focus && !this.disabled;
   }

   get labelQaTag(): string {
      return (this.qaTag || this.name) + '-label';
   }

   /** Style functions */
   onFocus(event: Event): void {
      this.focus = true;

      if (this.isVarValue) {
         this.selectVarMode = true;
         this.filterVarListValue(this.internalControl.value);
      }

   }

   onKeydown(event) {
         if (event.key === 'Backspace' && this.showVars) {
            this.internalControl.setValue('');
            this.selectVarMode = false;
            this.isVarValue = false;
            this.selectedIndex = 0;
         } else if (navigates.includes(event.key) && this.variableList && this.filteredVariableList.length ) {
            this.scrollList = this._element.querySelector('ul');
            switch (event.key) {
               case 'ArrowUp':
                  if (this.selectedIndex > 0) {
                     this.selectedIndex--;
                  }
                  break;
               case 'ArrowDown':

                  if (this.selectedIndex < this.filteredVariableList.length - 1) {
                     if (!this.selectVarMode) {
                        this.selectVarMode = true;
                        this.filterVarListValue(this.internalControl.value);
                     } else {
                        this.selectedIndex++;
                     }
                  }
                  break;
               case 'Enter':
                  if (this.selectVarMode) {
                     this.selectVar(event, this.filteredVariableList[this.selectedIndex]);
                  }

                  break;
               default:
                  break;
            }
            if (this.scrollList) {
               this.scrollList.scrollTop = this.getScroll(this.scrollList);
            }
         }


    }

   private getScroll(list: any): any {
      if (list && list.children.length) {
         const elementsHeight = list.children.length && Array.from(list.children)
            .filter((element, index) => index <= this.selectedIndex)
            .reduce((prev: number, next: any, i, elements) => prev + next.offsetHeight, 0);
         return (list.scrollHeight !== list.offsetHeight && elementsHeight > list.offsetHeight)
            ? elementsHeight - list.offsetHeight
            : 0;
      }
     return 0;
   }

   onFocusOut(event: Event): void {
      this.focus = false;
   }

   selectAll(event: Event) {
      event.stopPropagation();
      event.preventDefault();
      this.internalControl.setValue('');
      this.selectedFilter = undefined;
      this.filterVarListValue('');
   }

   onChangeEvent(event: Event): void {
      this._value = this.vc.first.nativeElement.value;
      this.change.emit(this.value);
      event.stopPropagation();
      event.preventDefault();
   }

   onChangeFilter(option) {
      this.selectedOption = option;
      this.filterVarListValue(this.internalControl.value);
   }

   // When status change call this function to check if have errors
   private checkErrors(control: FormControl): void {
      const errors: { [key: string]: any } = control.errors;
      this.errorMessage = this.getErrorMessage(errors);
      this._cd.markForCheck();
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
