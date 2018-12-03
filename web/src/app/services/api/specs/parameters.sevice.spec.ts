/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Store } from '@ngrx/store';
import { cloneDeep as _cloneDeep } from 'lodash';
import { USER_STORE_MOCK } from '@test/mocks/user';

const initialStoreState: any = _cloneDeep(USER_STORE_MOCK);
import { MockStore } from '@test/store-mock';

import { ParametersService } from './../parameters.service';

describe('[ParametersService]', () => {
   const mockStoreInstance: MockStore<any> = new MockStore(initialStoreState);

   beforeEach(() => {
      spyOn(mockStoreInstance, 'dispatch');
      spyOn(mockStoreInstance, 'select').and.callThrough();
      TestBed.configureTestingModule({
         imports: [HttpClientTestingModule],
         providers: [
            ParametersService,
            {
               provide: Store, useValue: mockStoreInstance
            }]
      });
   });

   describe('should be able to call execution services', () => {
      let service: ParametersService;
      let http: HttpTestingController;

      beforeEach(inject([ParametersService], (_parametersService: ParametersService) => {
         service = TestBed.get(ParametersService);
         http = TestBed.get(HttpTestingController);

      }));

      afterEach(() => {
         http.verify();
      });

      it('should can get global parameters', () => {
         const url = 'globalParameters';
         service.getGlobalParameters().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can save global parameters', () => {
         const url = 'globalParameters';
         const query = {};
         service.saveGlobalParameter(query).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         expect(req.request.body).toEqual(query);
         req.flush('OK');
      });


      it('should can update global parameter', () => {
         const url = 'globalParameters';
         const query = {};
         service.updateGlobalParameter(query).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('PUT');
         expect(req.request.body).toEqual(query);
         req.flush('OK');
      });


      it('should can delete a global parameter', () => {
         const url = 'globalParameters/variable/';
         const paramName = 'param';
         service.deleteGlobalParameter(paramName).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url + paramName);
         expect(req.request.method).toBe('DELETE');
         req.flush('OK');
      });

      it('should can get environment parameters', () => {
         const url = 'paramList/environment';
         service.getEnvironmentParameters().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can retrieve environment and context', () => {
         const url = 'paramList/environmentAndContexts';
         service.getEnvironmentAndContext().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can retrieve environment contexts', () => {
         const url = 'paramList/environmentContexts';
         service.getEnvironmentContexts().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can retrieve custom and contexts', () => {
         const url = 'paramList/parentAndContexts/';
         const customName = 'custom';
         service.getCustomAndContext(customName).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url +  customName);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can retrieve params list', () => {
         const url = 'paramList';
         service.getParamList().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can create params list', () => {
         const url = 'paramList';
         const query = {};
         service.createParamList(query).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         req.flush('OK');
      });


      it('should can update params list', () => {
         const url = 'paramList';
         const query = {};
         service.updateParamList(query).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('PUT');
         req.flush('OK');
      });
   });

});
