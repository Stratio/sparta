/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Store } from '@ngrx/store';

import { ComponentFixture, async, TestBed, tick, fakeAsync, discardPeriodicTasks } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA, DebugElement } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SpInputModule } from '../../../../shared/components/sp-input/sp-input.module';

import { WorkflowRenameModal } from './workflow-rename.component';
import { MockStore } from '@test/store-mock';
import { TranslateMockModule } from '@test/translate-stub';
import * as workflowActions from './../../actions/workflow-list';
import { ErrorMessagesService } from 'app/services';
import { FOLDER_SEPARATOR } from '@app/workflows/workflow-managing/workflow.constants';

const initialStateValue = {
   workflowsManaging: {
      workflowsManaging: {
         workflowList: [],
         openedWorkflow: null,
         selectedVersions: [],
         selectedVersionsData: [],
         showModal: true,
         currentLevel: {
            id: '940800b2-6d81-44a8-84d9-26913a2faea4',
            name: '/home',
            label: 'home'
         }
      },
      order: {
         sortOrder: {
            orderBy: 'name',
            type: 1
         },
         sortOrderVersions: {
            orderBy: 'version',
            type: 1
         }
      }
   }
};
describe('[WorkflowsRenameModalComponent]', () => {
   let component: WorkflowRenameModal;
   let fixture: ComponentFixture<WorkflowRenameModal>;
   let debugElement: DebugElement;
   const errorsServiceMock = {
      errors: ('inputErrors')
   };
   const mockStoreInstance: MockStore<any> = new MockStore(initialStateValue);
   beforeEach(async(() => {
      TestBed.configureTestingModule({
         declarations: [WorkflowRenameModal],
         imports: [
            FormsModule,
            SpInputModule,
            TranslateMockModule
         ],
         schemas: [NO_ERRORS_SCHEMA],
         providers: [
            {
               provide: Store, useValue: mockStoreInstance
            },
            {
               provide: ErrorMessagesService, useValue: errorsServiceMock
            }
         ],
      }).compileComponents();  // compile template and css
   }));

   beforeEach(() => {
      fixture = TestBed.createComponent(WorkflowRenameModal);
      component = fixture.componentInstance;
      debugElement = fixture.debugElement;
   });
   afterEach(() => {
      fixture.destroy();
   });
   describe('if the renamed entity type is a group', () => {
      beforeEach(() => {
         component.entityType = 'Group';
         component.entityName = '/home/folder';
         fixture.detectChanges();
      });

      it('should  initialize getting the name of the current folder and root of the parent folder', () => {
         expect(component.name).toBe('folder');
         expect(component.currentGroup).toBe('/home');
      });

      it('should can save the new name when the form is valid', () => {
         spyOn(mockStoreInstance, 'dispatch');
         component.name = 'folder2';
         component.updateEntity();
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(
            new workflowActions.RenameGroupAction({
               oldName: component.entityName,
               newName: component.currentGroup + FOLDER_SEPARATOR + component.name
            }));
      });

      it('should not can save when the group name is invalid', fakeAsync((done) => {
         fixture.whenStable().then(() => {
            component.name = '';
            fixture.detectChanges();
            tick();
            fixture.detectChanges();
            expect(component.renameForm.valid).toBeFalsy();
            discardPeriodicTasks();
            done();
         });
      }));
   });

   describe('if the renamed entity type is a workflow', () => {

   });
});

