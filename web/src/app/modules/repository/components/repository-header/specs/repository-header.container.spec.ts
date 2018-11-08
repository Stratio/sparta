/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Store } from '@ngrx/store';
import { Router } from '@angular/router';

import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { RepositoryHeaderContainer } from './../repository-header.container';
import { take } from 'rxjs/operators';

import { MockStore } from '@test/store-mock';
import * as workflowActions from './../../../actions/workflow-list';

let component: RepositoryHeaderContainer;
let fixture: ComponentFixture<RepositoryHeaderContainer>;

const routerStub = {
   navigate: jasmine.createSpy('navigate')
};

describe('RepositoryHeaderContainer]', () => {
   const fakeWorkflowList = [
      {
         id: 'id1',
         name: 'workflow1',
         group: '/home',
         version: 1,
         versions: [
            {
               id: 'id1',
               name: 'workflow1',
               group: '/home',
               version: 1,
            },
            {
               id: 'id2',
               name: 'workflow1',
               group: '/home',
               version: 0
            }
         ],

      },
      {
         id: 'id2',
         name: 'workflow1',
         group: '/home',
         version: 0
      },
      {
         id: 'id3',
         name: 'workflow3',
         group: '/home',
         version: 2
      }
   ];
   const initialStateValue = {
      workflowsManaging: {
         workflowsManaging: {
            workflowList: fakeWorkflowList,
            workflowsVersionsList: fakeWorkflowList,
            openedWorkflow: fakeWorkflowList[0],
            selectedVersions: [fakeWorkflowList[0].id],
            selectedVersionsData: [fakeWorkflowList[0]],
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
   const mockStoreInstance: MockStore<any> = new MockStore(initialStateValue);

   beforeEach(async(() => {
      TestBed.configureTestingModule({
         declarations: [RepositoryHeaderContainer],
         schemas: [NO_ERRORS_SCHEMA],
         providers: [
            {
               provide: Store, useValue: mockStoreInstance
            },
            {
               provide: Router, useValue: routerStub
            },
         ],
      }).compileComponents();  // compile template and css
   }));

   beforeEach(async(() => {
      fixture = TestBed.createComponent(RepositoryHeaderContainer);
      component = fixture.componentInstance;
   }));

   afterEach(() => {
      fixture.destroy();
   });

   xit('should get OnInit selected versions and workflows', () => {
      component.selectedVersionsData$.pipe(take(1)).subscribe((versions: Array<any>) => {
         expect(versions.length).toBe(1);
         expect(versions[0]).toEqual(fakeWorkflowList[0]);
      });
      component.workflowVersions$.pipe(take(1)).subscribe((workflows: Array<any>) => {
         expect(workflows.length).toBe(2);
      });
   });

   it('should can edit the selected version', () => {
      component.editVersion(component.selectedVersions[0]);
      expect(routerStub.navigate).toHaveBeenCalledWith(['wizard/edit', component.selectedVersions[0]]);
   });


   describe('should can display managing header actions', () => {

      beforeEach(() => {
         mockStoreInstance.next(initialStateValue);
         fixture.detectChanges();
         spyOn(mockStoreInstance, 'dispatch');
      });

      it('can dispatch download workflows action', () => {
         const expectedAction = new workflowActions.DownloadWorkflowsAction(component.selectedVersions);
         component.downloadWorkflows();
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });

      it('can dispatch select group action', () => {
         const group = {
            name: 'group',
            id: 'id',
            label: 'label'
         };
         const expectedAction = new workflowActions.ChangeGroupLevelAction(group);
         component.selectGroup(group);
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });

      it('can dispatch delete workflows action', () => {
         const expectedAction = new workflowActions.DeleteWorkflowAction();
         component.deleteWorkflows();
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });

      it('can dispatch delete versions action', () => {
         const expectedAction = new workflowActions.DeleteVersionAction();
         component.deleteVersions();
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });

   });

});

