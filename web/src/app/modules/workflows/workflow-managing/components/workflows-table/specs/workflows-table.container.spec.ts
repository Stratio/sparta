/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Store } from '@ngrx/store';
import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { WorkflowsManagingTableContainer } from './../workflows-table.container';
import * as workflowActions from './../../../actions/workflow-list';

import { MockStore } from '@test/store-mock';
import { Group } from '../../../models/workflows';

let component: WorkflowsManagingTableContainer;
let fixture: ComponentFixture<WorkflowsManagingTableContainer>;

describe('[WorkflowsManagingTableContainer]', () => {
   const fakeWorkflowList = [
      {
         name: 'workflow1',
         group: '/home',
         version: 1
      },
      {
         name: 'workflow1',
         group: '/home',
         version: 0
      },
      {
         name: 'workflow3',
         group: '/home'
      }
   ];
   const mockStoreInstance: MockStore<any> = new MockStore({
      workflowsManaging: {
         workflowsManaging: {
            workflowList: fakeWorkflowList,
            openedWorkflow: fakeWorkflowList[0]
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
   });

   beforeEach(async(() => {
      TestBed.configureTestingModule({
         declarations: [WorkflowsManagingTableContainer],
         schemas: [NO_ERRORS_SCHEMA],
         providers: [
            { provide: Store, useValue: mockStoreInstance }
         ],
      }).compileComponents();  // compile template and css
   }));

   beforeEach(() => {
      fixture = TestBed.createComponent(WorkflowsManagingTableContainer);
      component = fixture.componentInstance;
      fixture.detectChanges();

   });

   it('should get OnInit the current versions list', () => {
      component.workflowVersions$.take(1).subscribe((versions) => {
         expect(versions.length).toBe(2);
         expect(versions[0]).toEqual(fakeWorkflowList[1]);
         expect(versions[1]).toEqual(fakeWorkflowList[0]);
      });
   });

   describe('should can display managing table actions', () => {

      beforeEach(() => {
         spyOn(mockStoreInstance, 'dispatch');
      });

      it('can dispatch change order action', () => {
         const event = {
            orderBy: 'name',
            type: 0
         };
         const expectedAction = new workflowActions.ChangeOrderAction(event);
         component.changeOrder(event);
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });


      it('can dispatch change versions order action', () => {
         const event = {
            orderBy: 'name',
            type: 0
         };
         const expectedAction = new workflowActions.ChangeVersionsOrderAction(event);
         component.changeOrderVersions(event);
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });

      it('can dispatch select workflow action', () => {
         const name = 'workflow-name';
         const expectedAction = new workflowActions.SelectWorkflowAction(name);
         component.selectWorkflow(name);
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });

      it('can dispatch select group action', () => {
         const name = 'group-name';
         const expectedAction = new workflowActions.SelectGroupAction(name);
         component.selectGroup(name);
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });

      it('can dispatch select version action', () => {
         const id = 'version-id';
         const expectedAction = new workflowActions.SelectVersionAction(id);
         component.selectVersion(id);
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });


      it('can dispatch change group level action', () => {
         const group: Group = {
            id: 'id',
            name: 'name',
            label: 'label'
         };
         const expectedAction = new workflowActions.ChangeGroupLevelAction(group);
         component.changeFolder(group);
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });

      it('can dispatch show workflow versions action', () => {
         const fakeWorkflow = {
            id: 'worfklow-id',
            name: 'workflow-name',
            group: 'workflow-group'
         };
         const expectedAction = new workflowActions.ShowWorkflowVersionsAction({
            name: fakeWorkflow.name,
            group: fakeWorkflow.group
         });
         component.showWorkflowVersions(fakeWorkflow);
         expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
      });
   });
});
