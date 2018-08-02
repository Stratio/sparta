/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import { StBreadcrumbsModule, StModalService, StModalResponse } from '@stratio/egeo';
import { NO_ERRORS_SCHEMA, ChangeDetectionStrategy } from '@angular/core';
import { of } from 'rxjs/observable/of';
import { cloneDeep as _cloneDeep } from 'lodash';

import { TranslateMockModule, initTranslate } from '@test/translate-stub';
import { Router } from '@angular/router';
import { SharedModule } from '@app/shared';
import { Store } from '@ngrx/store';
import { MockStore } from '@test/store-mock';
import { WizardHeaderComponent } from '@app/wizard';
import { WIZARD_STORE_MOCK } from '@test/wizard-store-mock';
import { Location } from '@angular/common';


let component: WizardHeaderComponent;
let fixture: ComponentFixture<WizardHeaderComponent>;

let routeMock: Router;
let modalServiceMock: StModalService;
let mockLocation: Location;

describe('[WizardHeaderComponent]', () => {

   const initialStoreState: any = _cloneDeep(WIZARD_STORE_MOCK);
   const mockStoreInstance: MockStore<any> = new MockStore(initialStoreState);
   const originalSetTimeout = window.setTimeout;
   const originalClearTimeout = window.clearTimeout;
   window.setTimeout = function (funcToCall, millis) {
      funcToCall();
      return 0;
   };

   window.clearTimeout = function (id) {};
   beforeEach(async(() => {
      routeMock = jasmine.createSpyObj('Route', ['navigate']);
      modalServiceMock = jasmine.createSpyObj('StModalService', ['show']);
      (<jasmine.Spy> modalServiceMock.show).and.returnValue(of(StModalResponse.YES));
      spyOn(mockStoreInstance, 'dispatch');
      spyOn(mockStoreInstance, 'select').and.callThrough();
      mockLocation = jasmine.createSpyObj('Location', ['back']);
      TestBed.configureTestingModule({
         imports: [
            StBreadcrumbsModule,
            TranslateMockModule,
            SharedModule
         ],
         providers: [
            { provide: Router, useValue: routeMock },
            { provide: StModalService, useValue: modalServiceMock },
            { provide: Store, useValue: mockStoreInstance },
            { provide: Location, useValue: mockLocation }
         ],
         declarations: [WizardHeaderComponent],
         schemas: [NO_ERRORS_SCHEMA]
      })
      // remove this block when the issue #12313 of Angular is fixed
         .overrideComponent(WizardHeaderComponent, {
            set: { changeDetection: ChangeDetectionStrategy.Default }
         })
         .compileComponents();  // compile template and css
   }));

   beforeEach(() => {
      initTranslate();
      fixture = TestBed.createComponent(WizardHeaderComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      spyOn(component.onSaveWorkflow, 'emit');
   });

   afterEach(() => {
      mockStoreInstance.next(initialStoreState);
      window.setTimeout = originalSetTimeout;
      window.clearTimeout = originalClearTimeout;
   });

   describe('User can save the new workflow when workflow has been edited', () => {
      beforeEach(() => {
         component.isPristine = false;
         fixture.detectChanges();
      });

      it('When user clicks on the "save" button, save workflow event is emitted', () => {
            expect(fixture.nativeElement.querySelector('#save-button')).not.toBeNull();
            fixture.nativeElement.querySelector('#save-button').click();

            expect(component.onSaveWorkflow.emit).toHaveBeenCalled();
      });

      it('While wizard is loading, progress bar is displayed and button save is disabled', () => {
            let loadingWizardStore = _cloneDeep(initialStoreState);
            loadingWizardStore.wizard.wizard.loading = true;
            mockStoreInstance.next(loadingWizardStore);
            fixture.detectChanges();

            expect(fixture.nativeElement.querySelector('.loading-bar')).not.toBeNull();
            expect(fixture.nativeElement.querySelector('#save-button').disabled).toBeTruthy();

            let editedWizardStore = _cloneDeep(initialStoreState);
            editedWizardStore.wizard.wizard.loading = false;
            mockStoreInstance.next(editedWizardStore);
            fixture.detectChanges();

            expect(fixture.nativeElement.querySelector('.loading-bar')).toBeNull();
            expect(fixture.nativeElement.querySelector('#save-button').disabled).toBeFalsy();

      });
   });
});
