/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Store } from '@ngrx/store';

import { GlobalConfigService } from './../config.service';

describe('[ConfigService]', () => {
   const mockStoreInstance = jasmine.createSpyObj('store', ['dispatch']);

   beforeEach(() => TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
         GlobalConfigService,
         {
            provide: Store, useValue: mockStoreInstance
         }]
   }));

   describe('should be able to call global config services', () => {
      let service: GlobalConfigService;
      let http: HttpTestingController;

      beforeEach(inject([GlobalConfigService], (_globalConfigService: GlobalConfigService) => {
         service = TestBed.get(GlobalConfigService);
         http = TestBed.get(HttpTestingController);

      }));

      afterEach(() => {
         http.verify();
      });

      it('should can get executions', () => {
         const url = 'config';
         service.getConfig().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });
  });
});
