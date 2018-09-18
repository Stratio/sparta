/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Store } from '@ngrx/store';
import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { cloneDeep as _cloneDeep } from 'lodash';
import { Observable } from 'rxjs/Observable';
import { StModalService, StModalResponse, StSearchModule } from '@stratio/egeo';

import { MockStore } from '@test/store-mock';
import { EnvironmentComponent } from '@app/settings/environment/environment.component';
import { ENVIRONMENT_STORE_MOCK } from '@test/environment-store-mock';
import { TranslateMockModule } from '@test/translate-stub';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BreadcrumbMenuService, ErrorMessagesService } from 'services/*';
import { SpInputComponent } from '@app/shared/components/sp-input/sp-input.component';

let component: EnvironmentComponent;
let fixture: ComponentFixture<EnvironmentComponent>;
let modalServiceMock: StModalService;
let breadcrumbMenuServiceMock: BreadcrumbMenuService;
let errorMessagesServiceMock: any;

describe('[EnvironmentComponent]', () => {
   const initialStateValue = _cloneDeep(ENVIRONMENT_STORE_MOCK);
   const mockStoreInstance: MockStore<any> = new MockStore(initialStateValue);

   beforeEach(async(() => {
      modalServiceMock = jasmine.createSpyObj('StModalService', ['show']);
      (<jasmine.Spy> modalServiceMock.show).and.returnValue(Observable.of(StModalResponse.YES));
      breadcrumbMenuServiceMock = jasmine.createSpyObj('BreadcrumbMenuService', ['getOptions']);
      errorMessagesServiceMock = { errors: { inputErrors: [] } };

      TestBed.configureTestingModule({
         imports: [TranslateMockModule, FormsModule, ReactiveFormsModule, StSearchModule],
         declarations: [EnvironmentComponent, SpInputComponent],
         schemas: [NO_ERRORS_SCHEMA],
         providers: [
            { provide: Store, useValue: mockStoreInstance },
            { provide: StModalService, useValue: modalServiceMock },
            { provide: BreadcrumbMenuService, useValue: breadcrumbMenuServiceMock },
            { provide: ErrorMessagesService, useValue: errorMessagesServiceMock }
         ],
      }).compileComponents();  // compile template and css
   }));

   beforeEach(() => {
      fixture = TestBed.createComponent(EnvironmentComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
   });

   afterEach(() => {
      mockStoreInstance.next(initialStateValue);
   });

   it('User can download the current version of environment variable list', () => {
      let newEnvironmentListStore = _cloneDeep(initialStateValue);
      let newEnvironmentVar = {
         name: 'NEW_ENVIRONMENT_VAR',
         value: 'fake'
      };
      newEnvironmentListStore.environment.environment.environmentList.variables.push(newEnvironmentVar);
      mockStoreInstance.next(newEnvironmentListStore);

      spyOn(document.body, 'appendChild');
      const fakeDate = new Date().getDate();
      spyOn(Date.prototype, 'getTime').and.returnValue(fakeDate);
      fixture.nativeElement.querySelector('#download-env-button').click();
      let aElement = (<jasmine.Spy> document.body.appendChild).calls.mostRecent().args[0];

      expect(aElement.href).toEqual('data:text/json;charset=utf-8,' + JSON.stringify(newEnvironmentListStore.environment.environment.environmentList));
      expect(aElement.download).toEqual(fakeDate.toString() + '.json');
   });

   describe('User can upload a list of environment vars', () => {
      afterEach(() => {
         mockStoreInstance.next(initialStateValue);
      });

      it('If file does not have vars which are already in the list, all them are added', (done) => {
         const emptyEnvironmentListStore = _cloneDeep(initialStateValue);
         emptyEnvironmentListStore.environment.environment.environmentList.variables = [];
         mockStoreInstance.next(emptyEnvironmentListStore);
         fixture.detectChanges();

         const fakeEnvVarList = [{
            name: 'NEW_ENVIRONMENT_VAR',
            value: 'fake'
         }, {
            name: 'NEW_ENVIRONMENT_VAR_2',
            value: 'fake 2'
         }
         ];
         const newEnvironmentVarFile = {
            variables: fakeEnvVarList
         };
         component.uploadVariables([new Blob([JSON.stringify(newEnvironmentVarFile)], { type: 'json' })]).then(() => {
            fixture.detectChanges();

            const newEnvironmentVariableList = fixture.nativeElement.querySelectorAll('.list-field');
            expect(newEnvironmentVariableList.length).toEqual(initialStateValue.environment.environment.environmentList.variables.length + newEnvironmentVarFile.variables.length);
            done();
         });
      });

      it('If file has some vars which are already in the list, them will be added and deleted the original one', (done) => {
         const fakeEnvVarList = [{
            name: 'NEW_ENVIRONMENT_VAR',
            value: 'fake'
         }, {
            name: 'NEW_ENVIRONMENT_VAR_2',
            value: 'fake 2'
         }, {
            name: initialStateValue.environment.environment.environmentList.variables[0].name,
            value: 'new from file'
         }
         ];
         let newEnvironmentVarFile = { variables: [...fakeEnvVarList] };

         component.uploadVariables([new Blob([JSON.stringify(newEnvironmentVarFile)], { type: 'json' })]).then(() => {
            fixture.detectChanges();

            const newEnvironmentVariableList = fixture.nativeElement.querySelectorAll('.list-field');
            expect(newEnvironmentVariableList.length).toEqual(initialStateValue.environment.environment.environmentList.variables.length + 2);
            const propertyInputs = newEnvironmentVariableList[2].querySelectorAll('input');
            expect(propertyInputs[0].value).toEqual(initialStateValue.environment.environment.environmentList.variables[0].name);
            expect(propertyInputs[1].value).toEqual('new from file');

            done();
         })
      });

      it('If file has some vars with an empty value and it is already in the original list, the original one is added', (done) => {
         const fakeEnvVarList = [
            {
               name: initialStateValue.environment.environment.environmentList.variables[0].name,
               value: ''
            }
         ];
         let newEnvironmentVarFile = { variables: [...fakeEnvVarList] };

         component.uploadVariables([new Blob([JSON.stringify(newEnvironmentVarFile)], { type: 'json' })]).then(() => {
            fixture.detectChanges();

            const newEnvironmentVariableList = fixture.nativeElement.querySelectorAll('.list-field');
            expect(newEnvironmentVariableList.length).toEqual(initialStateValue.environment.environment.environmentList.variables.length);
            const propertyInputs = newEnvironmentVariableList[0].querySelectorAll('input');
            expect(propertyInputs[0].value).toEqual(initialStateValue.environment.environment.environmentList.variables[0].name);
            expect(propertyInputs[1].value).toEqual(initialStateValue.environment.environment.environmentList.variables[0].value);

            done();
         });

      });

      it('When environment vars have been uploaded, save button has to be activated', (done) => {
         expect(fixture.nativeElement.querySelector('#save-env-button').disabled).toBeTruthy();

         component.uploadVariables([new Blob([JSON.stringify({
            variables: [{
               name: 'NEW_ENVIRONMENT_VAR',
               value: 'fake'
            }]
         })], { type: 'json' })]).then(() => {
            fixture.detectChanges();
            expect(fixture.nativeElement.querySelector('#save-env-button').disabled).toBeFalsy();
            done();
         });
      });

   });

   it('When there are more than 15 environment vars, add element button is displayed two times', () => {
      expect(fixture.nativeElement.querySelectorAll('.add-element').length).toBe(1);
      let environmentListStore = _cloneDeep(initialStateValue);
      environmentListStore.environment.environment.environmentList.variables = [];
      while (environmentListStore.environment.environment.environmentList.variables.length < 16) {
         environmentListStore.environment.environment.environmentList.variables.push({ name: '', value: '' });
      }
      mockStoreInstance.next(environmentListStore);
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelectorAll('.add-element').length).toBe(2);
   });
});
