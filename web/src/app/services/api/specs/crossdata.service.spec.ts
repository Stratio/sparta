/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Store } from '@ngrx/store';

import { CrossdataService } from './../crossdata.service';

describe('[CrossdataService]', () => {
   const mockStoreInstance = jasmine.createSpyObj('store', ['dispatch']);

   beforeEach(() => TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
         CrossdataService,
         {
            provide: Store, useValue: mockStoreInstance
         }]
   }));

   describe('should be able to call global config services', () => {
      let service: CrossdataService;
      let http: HttpTestingController;

      beforeEach(inject([CrossdataService], (_crossdataService: CrossdataService) => {
         service = TestBed.get(CrossdataService);
         http = TestBed.get(HttpTestingController);

      }));

      afterEach(() => {
         http.verify();
      });

      it('should can get crossdata databases', () => {
         const url = 'crossdata/databases';
         service.getCrossdataDatabases().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can get crossdata tables', () => {
         const url = 'crossdata/tables';
         service.getCrossdataTables().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });

      it('should can get database tables', () => {
         const url = 'crossdata/tables';
         service.getCrossdataTables().subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('GET');
         req.flush('OK');
      });


      it('should can get crossdata table info', () => {
         const tableName = 'table';
         const url = 'crossdata/tables/info';
         service.getCrossdataTableInfo(tableName).subscribe(response => {
            expect(response).toEqual('OK');
         });
         const req = http.expectOne(url);
         expect(req.request.method).toBe('POST');
         req.flush('OK');
      });
  });
});
