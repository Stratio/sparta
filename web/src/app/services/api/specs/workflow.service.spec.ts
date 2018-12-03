/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Store } from '@ngrx/store';
import { cloneDeep as _cloneDeep } from 'lodash';

import { WorkflowService } from './../workflow.service';
import { USER_STORE_MOCK } from '@test/mocks/user';
import { MockStore } from '@test/store-mock';

const initialStoreState: any = _cloneDeep(USER_STORE_MOCK);

describe('[WorkflowService]', () => {
   const mockStoreInstance: MockStore<any> = new MockStore(initialStoreState);
   beforeEach(() => {
      spyOn(mockStoreInstance, 'dispatch');
      spyOn(mockStoreInstance, 'select').and.callThrough();
      TestBed.configureTestingModule({
         imports: [HttpClientTestingModule],
         providers: [
            WorkflowService,
            {
               provide: Store, useValue: mockStoreInstance
            }]
      })});

   describe('should be able to call worflows and groups api services', () => {

      let service: WorkflowService;
      let http: HttpTestingController;

      beforeEach(inject([WorkflowService], (_workflowService: WorkflowService) => {
         service = TestBed.get(WorkflowService);
         http = TestBed.get(HttpTestingController);

      }));

      afterEach(() => {
         http.verify();
      });

      it('should create group', () => {
         const url = 'groups';
         const groupName = 'group-name';
         service.createGroup(groupName).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         req.flush('OK');
      });

      it('should get workflows by group', () => {
         const groupId = '0';
         const url = 'workflows/findAllByGroupDto/' + groupId;
         service.getWorkflowsByGroup(groupId).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should get workflow list', () => {
         const url = 'workflows';
         service.getWorkflowList().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should get all groups', () => {
         const url = 'groups';
         service.getGroups().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can create groups', () => {
         const url = 'groups';
         const newGroup = '/home/group';
         service.createGroup(newGroup).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         expect(req.request.body).toEqual({
            name: newGroup
         });
         req.flush('OK');
      });

      it('should can delete group by name', () => {
         const groupName = '/home/group';
         const url = 'groups/deleteByName/';

         service.deleteGroupByName(groupName).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + groupName);
         expect(req.request.method).toBe('DELETE');
         req.flush('OK');
      });

      it('should can delete group by id', () => {
         const groupId = '0';
         const url = 'groups/deleteById/';

         service.deleteGroupById(groupId).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + groupId);
         expect(req.request.method).toBe('DELETE');
         req.flush('OK');
      });

      it('should can update group', () => {
         const group = {
            id: '0',
            name: '/home'
         };
         const url = 'groups';
         service.updateGroup(group).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('PUT');
         expect(req.request.body).toEqual(group);
         req.flush('OK');
      });

      it('should can download a workflow', () => {
         const workflowId = '0';
         const url = 'workflows/download/';
         service.downloadWorkflow(workflowId).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + workflowId);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can validate with execution context', () => {
         const data = {
            workflowId: '0'
         };
         const url = 'workflows/validateWithExecutionContext';
         service.validateWithExecutionContext(data).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         expect(req.request.body).toEqual(data);
         req.flush('OK');
      });

      it('should can run a workflow', () => {
         const workflowId = '0';
         const url = 'workflows/run/';
         service.runWorkflow(workflowId).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + workflowId);
         expect(req.request.method).toBe('POST');
         req.flush('OK');
      });

      it('should can run a workflow with params', () => {
         const config = {
            workflowId: '0'
         };
         const url = 'workflows/runWithExecutionContext';
         service.runWorkflowWithParams(config).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         expect(req.request.body).toEqual(config);
         req.flush('OK');
      });

      it('should can stop a workflow', () => {
         const status = {
            current: 'Stopped'
         };
         const url = 'workflowStatuses';
         service.stopWorkflow(status).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('PUT');
         expect(req.request.body).toEqual(status);
         req.flush('OK');
      });

      it('should can delete a workflow', () => {
         const workflowId = '0';
         const url = 'workflows/';
         service.deleteWorkflow(workflowId).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + workflowId);
         expect(req.request.method).toBe('DELETE');
         req.flush('OK');
      });

      it('should can delete a workflow list', () => {
         const workflowIdList = ['0', '1'];
         const url = 'workflows/list';
         service.deleteWorkflowList(workflowIdList).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('DELETE');
         expect(req.request.body).toEqual(workflowIdList);
         req.flush('OK');
      });

      it('should can retrieve execution info', () => {
         const executionId = '1';
         const url = 'workflowExecutions/';
         service.getWorkflowExecutionInfo(executionId).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + executionId);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can validate a workflow', () => {
         const workflow = {
            name: 'workflow-name'
         };
         const url = 'workflows/validate';
         service.validateWorkflow(workflow).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         expect(req.request.body).toEqual(workflow);
         req.flush('OK');
      });

      it('should can rename a workflow', () => {
         const query = {
            name: 'workflow-name',
            newName: 'workflow-name1'
         };
         const url = 'workflows/rename';
         service.renameWorkflow(query).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('PUT');
         expect(req.request.body).toEqual(query);
         req.flush('OK');
      });

      it('should can move a workflow', () => {
         const query = {
            name: 'workflow-name',
            newName: 'workflow-name1',
            group: '/home'
         };
         const url = 'workflows/move';
         service.moveWorkflow(query).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('PUT');
         expect(req.request.body).toEqual(query);
         req.flush('OK');
      });

      it('should can generate a workflow version', () => {
         const workflow = {
            name: 'workflow-name'
         };
         const url = 'workflows/version';
         service.generateVersion(workflow).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         expect(req.request.body).toEqual(workflow);
         req.flush('OK');
      });

      it('should can get run parameters', () => {
         const workflowId = '0';
         const url = 'workflows/runWithParametersViewById/';
         service.getRunParameters(workflowId).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + workflowId);
         expect(req.request.method).toBe('POST');
         req.flush('OK');
      });

      it('should can get run parameters from workflow', () => {
         const workflow = {
            name: 'workflow1'
         };
         const url = 'workflows/runWithParametersView';
         service.getRunParametersFromWorkflow(workflow).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url );
         expect(req.request.method).toBe('POST');
         expect(req.request.body).toEqual(workflow);
         req.flush('OK');
      });

   });
});
