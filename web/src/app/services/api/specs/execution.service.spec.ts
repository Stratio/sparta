/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Store } from '@ngrx/store';
import { cloneDeep as _cloneDeep } from 'lodash';

import { ROOT_STORE_MOCK } from '@test/root-store-mock';
import { MockStore } from '@test/store-mock';

import { ExecutionService } from './../execution.service';

const initialStoreState: any = _cloneDeep(ROOT_STORE_MOCK);

describe('[ExecutionService]', () => {
   const mockStoreInstance: MockStore<any> = new MockStore(initialStoreState);

   beforeEach(() => {
      spyOn(mockStoreInstance, 'dispatch');
      spyOn(mockStoreInstance, 'select').and.callThrough();
      TestBed.configureTestingModule({
         imports: [HttpClientTestingModule],
         providers: [
            ExecutionService,
            {
               provide: Store, useValue: mockStoreInstance
            }]
      });
   });

   describe('should be able to call execution services', () => {
      let service: ExecutionService;
      let http: HttpTestingController;

      beforeEach(inject([ExecutionService], (_executionService: ExecutionService) => {
         service = TestBed.get(ExecutionService);
         http = TestBed.get(HttpTestingController);

      }));

      afterEach(() => {
         http.verify();
      });

      it('should can get executions', () => {
         const url = 'workflowExecutions';
         service.getExecutions().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });


      it('should can get dashboard executions', () => {
         const url = 'workflowExecutions/dashboard';
         service.getDashboardExecutions().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can get dto executions', () => {
         const url = 'workflowExecutions/findAllDto';
         service.getAllExecutions().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can get executions by query', () => {
         const query = {
            archived: true
         };
         const url = 'workflowExecutions/findByQueryDto';
         service.getExecutionsByQuery(query).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         expect(req.request.body).toEqual(query);
         req.flush('OK');
      });

      it('should can archive execution', () => {
         const body = {
            executionId: '1',
            archived: true
         };
         const url = 'workflowExecutions/archived';
         service.archiveExecution(body.executionId, body.archived).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         expect(req.request.body).toEqual(body);
         req.flush('OK');
      });

      it('should can stop an executions by Id', () => {
         const executionId = '1';
         const url = 'workflowExecutions/stop/';
         service.stopExecutionsById(executionId).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + executionId);
         expect(req.request.method).toBe('POST');
         req.flush('OK');
      });

      it('should can get execution info', () => {
         const executionId = '1';
         const url = 'workflowExecutions/';
         service.getWorkflowExecutionInfo(executionId).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + executionId);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can delete an execution', () => {
         const executionId = '1';
         const url = 'workflowExecutions/';
         service.deleteExecution(executionId).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + executionId);
         expect(req.request.method).toBe('DELETE');
         req.flush('OK');
      });
   });

});
