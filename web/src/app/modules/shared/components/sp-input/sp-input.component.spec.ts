/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, DebugElement, OnInit } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { By } from '@angular/platform-browser';

import { SpInputComponent } from './sp-input.component';
import { SpInputError } from './sp-input.error.model';
import { SpInputModule } from './sp-input.module';
import { StLabelModule } from '@stratio/egeo';


let component: SpInputComponent;
let fixture: ComponentFixture<SpInputComponent>;
let input: HTMLInputElement;

describe('SpInputComponent', () => {
   beforeEach(async(() => {
      TestBed.configureTestingModule({
         imports: [FormsModule, ReactiveFormsModule, StLabelModule],
         declarations: [SpInputComponent]
      })
         .compileComponents();  // compile template and css
   }));

   beforeEach(() => {
      fixture = TestBed.createComponent(SpInputComponent);
      input = fixture.nativeElement.querySelector('input');
      component = fixture.componentInstance;
      component.qaTag = 'test qaTag';
   });

   it('Input should have a placeholder', () => {
      component.placeholder = 'Placeholder sample';
      fixture.detectChanges();
      expect(input.getAttribute('placeholder')).toContain('Placeholder sample');
   });

   it('Input can be a password Input', () => {
      component.fieldType = 'password';
      fixture.detectChanges();
      expect(input.getAttribute('type')).toContain('password');
   });

   it('Input can be a text Input', () => {
      component.fieldType = 'text';
      fixture.detectChanges();
      expect(input.getAttribute('type')).toContain('text');
   });

   it('Input can be a number Input', () => {
      component.fieldType = 'number';
      fixture.detectChanges();
      expect(input.getAttribute('type')).toContain('number');
   });

   it('Input has attribute value', () => {
      component.value = 'name';
      fixture.detectChanges();
      expect(input.value).toContain('name');
   });


   it('Input should be disabled', () => {
      fixture.detectChanges();

      component.setDisabledState(true);
      fixture.detectChanges();
      expect(input.disabled).toBe(true);

      component.setDisabledState(false);
      fixture.detectChanges();
      expect(input.disabled).toBe(false);
   });

   it('Input should be enabled', () => {
      fixture.detectChanges();
      component.setDisabledState(false);
      fixture.detectChanges();
      expect(input.disabled).toBe(false);
   });

   it('Input should be focused naturally', () => {
      fixture.detectChanges();
      input.focus();
      expect(component.focus).toBe(true);
   });

});

@Component({
   template: `
      <form [formGroup]="reactiveForm" novalidate autocomplete="off" (ngSubmit)="onSubmitReactiveForm()" class="col-md-6">
         <div class="form-group">
           <sp-input
                  label="Description"
                  placeholder="Module description"
                  [forceValidations]="forceValidations"
                  [errors]="errors"
                  name="description"
                  qaTag="description-input"
                  formControlName="description"
                  [fieldType]=fieldType
               ></sp-input>
         </div>
      </form>
      `
})
class FormReactiveComponent implements OnInit {
   public fieldType: string = 'string';
   public forceValidations: boolean;
   public reactiveForm: FormGroup;
   public minLength: number = 3;
   public maxLength: number = 20;
   public min: number = 10;
   public max: number = 100;
   public pattern: string = '[a-z]*';
   public model: any = {
      name: 'Egeo',
      description: '',
      components: 10
   };

   public errors: SpInputError = {
      generic: 'Error',
      required: 'This field is required',
      minLength: 'The field min length is: ' + this.minLength,
      maxLength: 'The field max length is: ' + this.maxLength,
      min: 'The number has to be higher than: ' + this.min,
      max: 'The number has to be minor than: ' + this.max,
      pattern: 'Invalid value'
   };

   constructor(private _fb: FormBuilder) { }

   ngOnInit(): void {
      this.reactiveForm = this._fb.group({
         description: [
            this.model.description,
            [
               Validators.required,
               Validators.minLength(this.minLength),
               Validators.maxLength(this.maxLength),
               Validators.pattern(this.pattern)
            ]
         ]
      });
   }

   disableInput(): void {
      this.reactiveForm.get('description').disable();
   }

   enableInput(): void {
      this.reactiveForm.get('description').enable();
   }


   onSubmitReactiveForm(): void { }
}

let reactiveFixture: ComponentFixture<FormReactiveComponent>;
let reactiveComp: FormReactiveComponent;

describe('SpInputComponent in reactive form', () => {
   beforeEach(async(() => {
      TestBed.configureTestingModule({
         imports: [FormsModule, ReactiveFormsModule, SpInputModule],
         declarations: [FormReactiveComponent]
      })
         .compileComponents();  // compile template and css
   }));

   beforeEach(() => {
      reactiveFixture = TestBed.createComponent(FormReactiveComponent);
      reactiveComp = reactiveFixture.componentInstance;
   });

   afterEach(() => {
      reactiveFixture.destroy();
   });

   it('should be init correct', () => {
      reactiveFixture.detectChanges();
      let htmlInput: HTMLInputElement = reactiveFixture.debugElement.query(By.css('input')).nativeElement;

      expect(htmlInput.placeholder).toBe('Module description');
   });

   it('should notify custom errors', () => {
      reactiveComp.forceValidations = false;
      reactiveFixture.detectChanges();
      let htmlInput: HTMLInputElement = reactiveFixture.debugElement.query(By.css('input')).nativeElement;

      reactiveComp.forceValidations = true;
      reactiveFixture.detectChanges();

      // Required
      let errorMessage: DebugElement = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('This field is required');

      // Min length
      htmlInput.value = 'a';
      htmlInput.dispatchEvent(new Event('input'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('The field min length is: 3');


      // Max length
      htmlInput.value = 'abcdefghijklmnopqrstuvwxyz';
      htmlInput.dispatchEvent(new Event('input'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('The field max length is: 20');


      // Max length
      htmlInput.value = '1234567890';
      htmlInput.dispatchEvent(new Event('input'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('Invalid value');
   });

   it('should notify generic errors', () => {
      let genericError: string = 'Generic Error';
      reactiveComp.forceValidations = false;
      reactiveComp.errors = { generic: genericError };
      reactiveFixture.detectChanges();
      let htmlInput: HTMLInputElement = reactiveFixture.debugElement.query(By.css('input')).nativeElement;

      reactiveComp.forceValidations = true;
      reactiveFixture.detectChanges();

      // Required
      let errorMessage: DebugElement = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual(genericError);

      // Min length
      htmlInput.value = 'a';
      htmlInput.dispatchEvent(new Event('input'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual(genericError);


      // Max length
      htmlInput.value = 'abcdefghijklmnopqrstuvwxyz';
      htmlInput.dispatchEvent(new Event('input'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual(genericError);


      // Max length
      htmlInput.value = '1234567890';
      htmlInput.dispatchEvent(new Event('input'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual(genericError);
   });

   it('should notify empty error', () => {
      reactiveComp.forceValidations = false;
      reactiveComp.errors = {};
      reactiveFixture.detectChanges();
      let htmlInput: HTMLInputElement = reactiveFixture.debugElement.query(By.css('input')).nativeElement;

      reactiveComp.forceValidations = true;
      reactiveFixture.detectChanges();

      // Required
      let errorMessage: DebugElement = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('');

      // Min length
      htmlInput.value = 'a';
      htmlInput.dispatchEvent(new Event('input'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('');


      // Max length
      htmlInput.value = 'abcdefghijklmnopqrstuvwxyz';
      htmlInput.dispatchEvent(new Event('input'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('');


      // Max length
      htmlInput.value = '1234567890';
      htmlInput.dispatchEvent(new Event('input'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('');
   });

   it('should be able to disable and enable', () => {
      reactiveComp.forceValidations = false;
      reactiveFixture.detectChanges();

      reactiveComp.enableInput();
      reactiveFixture.detectChanges();
      let htmlInput: HTMLInputElement = reactiveFixture.debugElement.query(By.css('input')).nativeElement;
      expect(htmlInput).toBeDefined();
      expect(htmlInput.classList).not.toContain('disabled');

      reactiveComp.disableInput();
      reactiveFixture.detectChanges();
      htmlInput = reactiveFixture.debugElement.query(By.css('input')).nativeElement;
      expect(htmlInput).toBeDefined();
      expect(htmlInput.disabled).toBeTruthy();

      reactiveComp.enableInput();
      reactiveFixture.detectChanges();
      htmlInput = reactiveFixture.debugElement.query(By.css('input')).nativeElement;
      expect(htmlInput).toBeDefined();
      expect(htmlInput.disabled).toBeFalsy();
   });

   it('should notify empty error with undefined errors object', () => {
      reactiveComp.forceValidations = false;
      reactiveComp.errors = undefined;
      reactiveFixture.detectChanges();
      let htmlInput: HTMLInputElement = reactiveFixture.debugElement.query(By.css('input')).nativeElement;

      reactiveComp.forceValidations = true;
      reactiveFixture.detectChanges();

      // Required
      let errorMessage: DebugElement = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('');


      // All ok
      htmlInput.value = 'prueba';
      htmlInput.dispatchEvent(new Event('input'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeNull();
   });


   it('should notify no notify error with focus', () => {
      reactiveComp.forceValidations = false;
      reactiveComp.errors = { generic: 'error' };
      reactiveFixture.detectChanges();
      let htmlInput: HTMLInputElement = reactiveFixture.debugElement.query(By.css('input')).nativeElement;

      reactiveComp.forceValidations = true;
      reactiveFixture.detectChanges();

      // Required
      let errorMessage: DebugElement = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect(errorMessage.nativeElement).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('error');

      htmlInput.dispatchEvent(new Event('focus'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeNull();

      htmlInput.dispatchEvent(new Event('blur'));
      reactiveFixture.detectChanges();

      errorMessage = reactiveFixture.debugElement.query(By.css('.st-input-error-message'));
      expect(errorMessage).toBeDefined();
      expect((<HTMLSpanElement>errorMessage.nativeElement).textContent).toEqual('error');
   });
});


// TODO: TEST INPUT IN TEMPLATE FORM