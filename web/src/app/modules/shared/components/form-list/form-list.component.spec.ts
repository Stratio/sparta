/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { async, TestBed, ComponentFixture } from '@angular/core/testing';
import { SharedModule } from '@app/shared';
import { FormListComponent } from '@app/shared/components/form-list/form-list.component';
import { FormsModule, ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { SpInputModule } from '@app/shared/components/sp-input/sp-input.module';
import { EgeoModule, StCheckboxModule, StTextareaModule } from '@stratio/egeo';

import { SpSelectModule } from '../sp-select/sp-select.module';
import { ErrorMessagesService } from '../../../../services';
import { FormGroup, FormArray, Validators } from '@angular/forms';
import { DebugElement, Component, OnInit } from '@angular/core';
import { By } from '@angular/platform-browser';
import { SpTextareaModule } from '@app/shared/components/sp-textarea/sp-textarea.module';
import { HighlightTextareaModule } from '../highlight-textarea/hightlight-textarea.module';
import { SpInputError } from '@app/shared/components/sp-input/sp-input.models';

let component: FormListComponent;
let fixture: ComponentFixture<FormListComponent>;
let el: DebugElement;

const model = [
    {
        host: 'http://host.com',
        port: '8080'
    },
    {
        host: 'http://host2.com',
        port: '3000'
    }
];

const inputSchema = [
    {
        'propertyId': 'host',
        'propertyName': '_HOST_',
        'propertyType': 'text',
        'regexp': '((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))|(((?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{2,63}))',
        'default': 'localhost',
        'required': true,
        'width': 6,
        'tooltip': 'Server address.',
        'hidden': false,
        'qa': 'fragment-details-stratio-kafkadirect-broker'
    },
    {
        'propertyId': 'port',
        'propertyName': '_PORT_',
        'propertyType': 'text',
        'regexp': '(0|([1-9]\\d{0,3}|[1-5]\\d{4}|[6][0-5][0-5]([0-2]\\d|[3][0-5])))',
        'default': '9092',
        'required': true,
        'width': 2,
        'tooltip': 'Server port.',
        'hidden': false,
        'qa': 'fragment-details-stratio-kafkadirect-port'
    }
];

describe('FormListComponent', () => {
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                FormsModule,
                ReactiveFormsModule,
                TranslateModule.forRoot(),
                HighlightTextareaModule,
                SpTextareaModule,
                SpInputModule,
                SpSelectModule,
                StCheckboxModule,
                StTextareaModule
            ],
            declarations: [FormListComponent],
            providers: [ErrorMessagesService]
        }).compileComponents();  // compile template and css
    }));

    beforeEach(async () => {
        fixture = TestBed.createComponent(FormListComponent);
        component = fixture.componentInstance;
        component.qaTag = 'test qaTag';
        el = fixture.debugElement;
        component.formListData = inputSchema;
    });

    it('should create a `FormGroup` comprised with an empty form array', () => {
        component.ngOnInit();
        expect(component.internalControl instanceof FormGroup).toBe(true);
    });

    it('should create an empty form array `items` inside the form group', () => {
        component.ngOnInit();
        const formArray: any = component.internalControl.controls['items'];
        expect(formArray instanceof FormArray).toBe(true);
        expect(formArray.controls.length).toBe(0);
    });

    describe('when its initialized', () => {

        beforeEach(async () => {
            fixture.detectChanges();
        });

        it('should add an item when add button is clicked', () => {
            expect(component.items.controls.length).toBe(0);

            component.addItem();
            expect(component.items.controls.length).toBe(1);

            component.addItem();
            expect(component.items.controls.length).toBe(2);
        });

        it('should delete item from model when a row is deleted', () => {
            component.addItem();
            component.addItem();
            component.addItem();
            component.deleteItem(2);
            expect(component.items.length).toBe(2);
        });

        xit('each added item fields should be the same than in the schema', () => {
            component.addItem();
            const item: any = component.items.controls[0];
            component.formListData.fields.map((field: any) => {
                expect(item.controls[field.propertyId]).toBeDefined();
            });
        });

        it('should add items when model is updated from writeValue', () => {
            component.writeValue(model);
            expect(component.items.controls.length).toBe(model.length);
            const itemValue = component.items.controls[0].value;
            expect(itemValue.host).toBe(model[0].host);
            expect(itemValue.port).toBe(model[0].port);
        });
    });
});


@Component({
    template: `
      <form [formGroup]="reactiveForm" novalidate autocomplete="off" (ngSubmit)="onSubmitReactiveForm()" class="col-md-6">
         <div class="form-group">
          <form-list [formListData]="inputSchema"
            qaTag="description-input"
            formControlName="kafka"
            name="kafka"
            [errors]="errors"
            [forceValidations]="forceValidations" 
            label="kafka"></form-list>
         </div>
      </form>
      `
})
class FormReactiveComponent implements OnInit {
    public forceValidations: boolean;
    public reactiveForm: FormGroup;
    public inputSchema: any = inputSchema;
    public model: any = {
        name: 'Sparta',
        kafka: [
            {
                host: 'www.host.com',
                port: '3000'
            }
        ],
        components: 10
    };

    public errors: SpInputError = {
        required: 'This field is required',
    };

    constructor(private _fb: FormBuilder) { }

    ngOnInit(): void {
        this.reactiveForm = this._fb.group({
            kafka: [
                this.model.kafka,
                [
                    Validators.required
                ]
            ]
        });
    }

    disableInput(): void {
        this.reactiveForm.get('kafka').disable();
    }

    enableInput(): void {
        this.reactiveForm.get('kafka').enable();
    }


    onSubmitReactiveForm(): void { }
}


let reactiveFixture: ComponentFixture<FormReactiveComponent>;
let reactiveComp: FormReactiveComponent;

describe('FormListComponent in reactive form', () => {
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [FormsModule, ReactiveFormsModule, SharedModule, TranslateModule.forRoot(), SpInputModule, SpSelectModule],
            declarations: [FormReactiveComponent],
            providers: [ErrorMessagesService]
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

    describe('should notify required error', () => {
        it('when there are not any elements', () => {
            reactiveComp.model.kafka = [];
            reactiveComp.forceValidations = true;
            reactiveFixture.detectChanges();
            let errorMessage: DebugElement = reactiveFixture.debugElement.query(By.css('.st-input-error-layout'));
            expect(errorMessage).toBeDefined();
        });

        xit('when a required field is empty', () => {
            reactiveComp.model.kafka = [
                {
                    host: '', // empty required field
                    port: '3000'
                }
            ];
            reactiveComp.forceValidations = true;
            reactiveFixture.detectChanges();
            let errorMessage: DebugElement = reactiveFixture.debugElement.query(By.css('.st-input-error-layout'));
            expect(errorMessage).toBeDefined();
        });

    });

    it('should be able to disable and enable', () => {
        reactiveComp.forceValidations = false;
        reactiveFixture.detectChanges();

        reactiveComp.enableInput();
        reactiveFixture.detectChanges();
        const htmlInput: HTMLInputElement = reactiveFixture.debugElement.query(By.css('input')).nativeElement;
        expect(htmlInput).toBeDefined();
        expect(htmlInput.classList).not.toContain('disabled');
    });

});
