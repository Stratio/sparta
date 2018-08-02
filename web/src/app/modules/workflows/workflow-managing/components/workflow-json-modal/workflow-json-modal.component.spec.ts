/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import { StBreadcrumbsModule, StModalService, StModalResponse, StTextareaModule } from '@stratio/egeo';
import { NO_ERRORS_SCHEMA, ChangeDetectionStrategy } from '@angular/core';
import { of } from 'rxjs/observable/of';
import { cloneDeep as _cloneDeep } from 'lodash';
import { Location } from '@angular/common';
import { Store } from '@ngrx/store';

import { TranslateMockModule, initTranslate } from '@test/translate-stub';
import { Router } from '@angular/router';
import { SharedModule } from '@app/shared';
import { MockStore } from '@test/store-mock';
import * as workflowActions from './../../actions/workflow-list';
import { WorkflowJsonModal } from '@app/workflows/workflow-managing';
import { ROOT_STORE_MOCK } from '@test/root-store-mock';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';


let component: WorkflowJsonModal;
let fixture: ComponentFixture<WorkflowJsonModal>;

let routeMock: Router;
let modalServiceMock: StModalService;
let mockLocation: Location;

const initialStoreState: any = _cloneDeep(ROOT_STORE_MOCK);

describe('[WorkflowJsonModal]', () => {

   const mockStoreInstance: MockStore<any> = new MockStore(initialStoreState);
   const fakeWorkflowJson = {name: 'fake name', description: 'fake description'};

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
            SharedModule,
            FormsModule,
            StTextareaModule
         ],
         providers: [
            { provide: Store, useValue: mockStoreInstance }
         ],
         declarations: [WorkflowJsonModal],
         schemas: [NO_ERRORS_SCHEMA]
      })
      // remove this block when the issue #12313 of Angular is fixed
         .overrideComponent(WorkflowJsonModal, {
            set: { changeDetection: ChangeDetectionStrategy.Default }
         })
         .compileComponents();  // compile template and css
   }));


   beforeEach(() => {
      initTranslate();
      fixture = TestBed.createComponent(WorkflowJsonModal);
      component = fixture.componentInstance;
      fixture.detectChanges();
   });

   describe('User can save the new workflow when workflow json has been typed', () => {

      beforeEach(() => {
         component.model.json = JSON.stringify(fakeWorkflowJson);
         fixture.detectChanges();
      });

      afterEach(() => {
         mockStoreInstance.next(initialStoreState);
      });

      it('When user clicks on the "save" button, save workflow action is emitted', () => {
         fixture.nativeElement.querySelector('#save-button').click();

        expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(new workflowActions.SaveJsonWorkflowAction(fakeWorkflowJson));
      });

      it('While workflow is being saved, button save is disabled', () => {
         let savingWorkflowStore = _cloneDeep(initialStoreState);
         savingWorkflowStore.workflowsManaging.workflowsManaging.saving = true;
         mockStoreInstance.next(savingWorkflowStore);
         fixture.detectChanges();

         expect(fixture.nativeElement.querySelector('#save-button').disabled).toBeTruthy();
         let completedWorkflowStore = _cloneDeep(initialStoreState);
         completedWorkflowStore.workflowsManaging.workflowsManaging.saving = false;
         mockStoreInstance.next(completedWorkflowStore);
         fixture.detectChanges();

         expect(fixture.nativeElement.querySelector('#save-button').disabled).toBeFalsy();
      });
   });
});
