/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Store } from '@ngrx/store';

import { WizardApiService } from './../wizard.service';

describe('[WizardService]', () => {
  const mockStoreInstance = jasmine.createSpyObj('store', ['dispatch']);

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      WizardApiService,
      {
        provide: Store, useValue: mockStoreInstance
      }]
  }));

  describe('should be able to call wizard services', () => {
    let service: WizardApiService;
    let http: HttpTestingController;

    beforeEach(inject([WizardApiService], (_executionService: WizardApiService) => {
      service = TestBed.get(WizardApiService);
      http = TestBed.get(HttpTestingController);

    }));

    afterEach(() => {
      http.verify();
    });

    it('should can debug a workflow', () => {
      const workflow = {
        name: 'workflow'
      };
      const url = 'debug';
      service.debug(workflow).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        workflowOriginal: workflow
      });
      req.flush('OK');
    });

    it('should can debug with execution context', () => {
      const request = {
        workflowId: '1',
        executionContext: {}
      };
      const url = 'debug/runWithExecutionContext';
      service.debugWithExecutionContext(request.workflowId, request.executionContext).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush('OK');
    });


    it('should can run a debug', () => {
      const workflowId = '1';
      const url = 'debug/run/';
      service.runDebug(workflowId).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url + workflowId);
      expect(req.request.method).toBe('POST');
      req.flush('OK');
    });

    it('should retrieve the debug result', () => {
      const workflowId = '1';
      const url = 'debug/resultsById/';
      service.getDebugResult(workflowId).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url + workflowId);
      expect(req.request.method).toBe('GET');
      req.flush('OK');
    });

    it('should upload a debug mock file', () => {
      const workflowId = '1';
      const file = '';

      const url = 'debug/uploadFile/';
      service.uploadDebugFile(workflowId, file).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url + workflowId);
      expect(req.request.method).toBe('POST');
      req.flush('OK');
    });

    it('should delete a debug mock file', () => {
      const path = '/home';

      const url = 'debug/deleteFile/';
      service.deleteDebugFile(path).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url + path);
      expect(req.request.method).toBe('DELETE');
      req.flush('OK');
    });

    it('should download a debug mock file', () => {
      const path = '/home';

      const url = 'debug/downloadFile/';
      service.downloadDebugFile(path).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url + path);
      expect(req.request.method).toBe('GET');
      req.flush('OK');
    });
  });

});
